package day14;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class DayFourteenTest {

	private static final String SAMPLE = """
			NNCB

			CH -> B
			HH -> N
			CB -> H
			NH -> C
			HB -> C
			HC -> B
			HN -> C
			NN -> C
			BH -> H
			NC -> B
			NB -> B
			BN -> B
			BB -> N
			BC -> B
			CC -> N
			CN -> C
			""";

	@Test
	void partOneSample() throws Exception {
		assertThat(subtractLeastCommonFromMostCommonAfterIterations(SAMPLE, 10) / 2 + 1).isEqualTo(1588);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(subtractLeastCommonFromMostCommonAfterIterations(input, 10) / 2).isEqualTo(3831);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(subtractLeastCommonFromMostCommonAfterIterations(SAMPLE, 40) / 2 + 1).isEqualTo(2188189693529L);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(subtractLeastCommonFromMostCommonAfterIterations(input, 40) / 2).isEqualTo(5725739914282L);
	}

	private static long subtractLeastCommonFromMostCommonAfterIterations(String input, int iterations) {
		Polymer polymer = Polymer.parse(input);
		for (int i = 0; i < iterations; i++) {
			polymer = polymer.apply();
		}

		List<Long> list = polymer.pairs()
				.entrySet()
				.stream()
				.flatMap(entry -> Stream.of(
						Map.entry(entry.getKey().left(), entry.getValue()),
						Map.entry(entry.getKey().right(), entry.getValue())))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum))
				.values()
				.stream()
				.sorted()
				.toList();
		return list.get(list.size() - 1) - list.get(0);
	}

}

record Polymer(Map<Pair, Long> pairs, Map<Pair, String> rules) {

	static Polymer parse(String input) {
		String[] split = input.split("\n\n");
		return new Polymer(
				parsePairs(split[0]),
				parseRules(split[1]));
	}

	private static Map<Pair, Long> parsePairs(String split) {
		Map<Pair, Long> pairs = new HashMap<>();
		String[] elements = split.split("");
		for (int i = 0; i < elements.length - 1; i++) {
			String left = elements[i];
			String right = elements[i + 1];
			Pair pair = new Pair(left, right);
			pairs.merge(pair, 1L, Long::sum);
		}
		return pairs;
	}

	private static Map<Pair, String> parseRules(String rules) {
		return Stream.of(rules.split("\n"))
				.map(Rule::parse)
				.collect(toMap(Rule::pair, Rule::inserted));
	}

	Polymer apply() {
		Map<Pair, Long> next = new HashMap<>();

		for (Entry<Pair, Long> entry : pairs.entrySet()) {
			Pair pair = entry.getKey();
			Long count = entry.getValue();

			String inserted = rules.get(pair);
			Pair left = new Pair(pair.left(), inserted);
			Pair right = new Pair(inserted, pair.right());

			next.merge(left, count, Long::sum);
			next.merge(right, count, Long::sum);
		}

		return new Polymer(next, rules);
	}

}

record Rule(Pair pair, String inserted) {

	static Rule parse(String line) {
		String[] split = line.split(" -> ");
		return new Rule(Pair.parse(split[0]), split[1]);
	}

}

record Pair(String left, String right) {

	static Pair parse(String pair) {
		return new Pair(pair.substring(0, 1), pair.substring(1));
	}

	@Override
	public String toString() {
		return left + right;
	}

}
