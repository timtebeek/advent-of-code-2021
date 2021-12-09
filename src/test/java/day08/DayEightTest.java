package day08;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

class DayEightTest {

	private static final String SAMPLE = """
			be cfbegad cbdgef fgaecd cgeb fdcge agebfd fecdb fabcd edb | fdgacbe cefdb cefbgd gcbe
			edbfga begcd cbg gc gcadebf fbgde acbgfd abcde gfcbed gfec | fcgedb cgb dgebacf gc
			fgaebd cg bdaec gdafb agbcfd gdcbef bgcad gfac gcb cdgabef | cg cg fdcagb cbg
			fbegcd cbd adcefb dageb afcb bc aefdc ecdab fgdeca fcdbega | efabcd cedba gadfec cb
			aecbfdg fbg gf bafeg dbefa fcge gcbea fcaegb dgceab fcbdga | gecf egdcabf bgf bfgea
			fgeab ca afcebg bdacfeg cfaedg gcfdb baec bfadeg bafgc acf | gebdcfa ecba ca fadegcb
			dbcfg fgd bdegcaf fgec aegbdf ecdfab fbedc dacgb gdcebf gf | cefg dcbef fcge gbcadfe
			bdfegc cbegaf gecbf dfcage bdacg ed bedf ced adcbefg gebcd | ed bcgafe cdgba cbgef
			egadfb cdbfeg cegd fecab cgb gbdefca cg fgcdab egfdb bfceg | gbdfcae bgc cg cgb
			gcafb gcf dcaebfg ecagb gf abcdeg gaef cafbge fdbac fegbdc | fgae cfgab fg bagce
			""";

	@Test
	void partOneSample() throws Exception {
		assertThat(countNumbersPartOne(SAMPLE)).isEqualTo(26);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countNumbersPartOne(input)).isEqualTo(367);
	}

	private static long countNumbersPartOne(String sample) {
		return Stream.of(sample.split("\n"))
				.map(line -> line.split(" \\| ")[1])
				.flatMap(numbers -> Stream.of(numbers.split(" ")))
				.filter(number -> number.length() != 5 && number.length() != 6)
				.count();
	}

	@Test
	void partTwoSampleLine() throws Exception {
		assertThat(addAllOutputValues(
				"acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab | cdfeb fcadb cdfeb cdbaf"))
						.isEqualTo(5353);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(addAllOutputValues(SAMPLE)).isEqualTo(61229);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(addAllOutputValues(input)).isEqualTo(974512);
	}

	private static int addAllOutputValues(String sample) {
		return Stream.of(sample.split("\n"))
				.map(line -> line.split(" \\| "))
				.mapToInt(split -> Wiring.parse(split[0]).interpret(split[1]))
				.sum();
	}

}

record Wiring(Map<String, Integer> sortedSignalLinesToRenderedNumber) {

	static Wiring parse(String line) {
		Map<Integer, List<String>> groupedBySize = Stream.of(line.split(" "))
				.map(Wiring::sortSegments)
				.collect(groupingBy(String::length));

		String one = groupedBySize.get(2).get(0);
		String four = groupedBySize.get(4).get(0);

		// 0, 6 & 9
		List<String> lengthSix = groupedBySize.get(6);
		String six = lengthSix.stream().filter(abc -> !fullyOverlapsWith(one, abc)).findFirst().get();
		List<String> zeroAndNine = lengthSix.stream().filter(Predicate.not(six::equals)).toList();
		String nine = zeroAndNine.stream().filter(abc -> fullyOverlapsWith(four, abc)).findFirst().get();
		String zero = zeroAndNine.stream().filter(abc -> !fullyOverlapsWith(four, abc)).findFirst().get();

		// 2, 3 & 5
		List<String> lengthFive = groupedBySize.get(5);
		String three = lengthFive.stream().filter(d -> fullyOverlapsWith(one, d)).findFirst().get();
		List<String> twoAndFive = lengthFive.stream().filter(d -> !fullyOverlapsWith(one, d)).toList();
		String two = twoAndFive.stream().filter(abc -> !fullyOverlapsWith(abc, six)).findFirst().get();
		String five = twoAndFive.stream().filter(abc -> fullyOverlapsWith(abc, six)).findFirst().get();

		return new Wiring(Map.of(
				one, 1,
				two, 2,
				three, 3,
				four, 4,
				five, 5,
				six, 6,
				groupedBySize.get(3).get(0), 7,
				groupedBySize.get(7).get(0), 8,
				nine, 9,
				zero, 0));
	}

	private static boolean fullyOverlapsWith(String query, String target) {
		List<String> charsInTarget = Stream.of(target.split("")).toList();
		return Stream.of(query.split("")).allMatch(charsInTarget::contains);
	}

	int interpret(String readings) {
		return Stream.of(readings.split(" "))
				.map(Wiring::sortSegments)
				.mapToInt(sortedSignalLinesToRenderedNumber::get)
				.reduce((a, b) -> a * 10 + b)
				.getAsInt();
	}

	static String sortSegments(String segments) {
		return segments.chars()
				.mapToObj(Character::toString)
				.sorted()
				.collect(joining());
	}

}
