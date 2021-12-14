package day14;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
		assertThat(something(SAMPLE, 10)).isEqualTo(1588);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(something(input, 10)).isEqualTo(3831);
	}

	@Test
	@Disabled
	void partTwoSample() throws Exception {
		assertThat(something(SAMPLE, 40)).isEqualTo(2188189693529L);
	}

	private static long something(String input, int iterations) {
		String[] split = input.split("\n\n");
		String template = split[0];
		Map<Pair, String> rules = parseRules(split);
		Polymer polymer = Polymer.parse(template);
		for (int i = 0; i < iterations; i++) {
			System.out.println(i);
			polymer.apply(rules);
		}

		Map<String, Long> counts = Stream.of(polymer.toString().split(""))
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		List<Long> list = counts.values().stream().sorted().toList();
		return list.get(list.size() - 1) - list.get(0);
	}

	private static Map<Pair, String> parseRules(String[] split) {
		return Stream.of(split[1].split("\n"))
				.map(Rule::parse)
				.collect(toMap(Rule::pair, Rule::inserted));
	}

}

record Polymer(Element start) {

	static Polymer parse(String template) {
		String[] split = template.split("");
		Element start = new Element(split[0], null);
		Element current = start;
		for (int i = 1; i < split.length; i++) {
			Element right = new Element(split[i], null);
			current.setRight(right);
			current = right;
		}
		return new Polymer(start);
	}

	void apply(Map<Pair, String> rules) {
		Element left = start;
		while (true) {
			Element right = left.getRight();
			if (right == null) {
				break;
			}
			Pair pair = new Pair(left.getName(), right.getName());
			left.setRight(new Element(rules.get(pair), right));
			left = right;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Element current = start;
		while (current != null) {
			sb.append(current.getName());
			current = current.getRight();
		}
		return sb.toString();
	}

}

class Element {
	private String name;
	private Element right;

	public Element(String name, Element right) {
		this.name = name;
		this.right = right;
	}

	public String getName() {
		return name;
	}

	public Element getRight() {
		return right;
	}

	public void setRight(Element right) {
		this.right = right;
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

}
