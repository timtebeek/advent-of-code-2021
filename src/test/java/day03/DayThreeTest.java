package day03;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class DayThreeTest {

	private static final String[] SAMPLE = """
			00100
			11110
			10110
			10111
			10101
			01111
			00111
			11100
			10000
			11001
			00010
			01010
			""".split("\n");

	@Test
	void partOneSample() throws Exception {
		assertThat(calculatePowerConsumption(SAMPLE)).isEqualTo(198);
	}

	@Test
	void partOneInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(calculatePowerConsumption(lines.toArray(String[]::new))).isEqualTo(3885894);
	}

	private static int calculatePowerConsumption(String[] input) {
		char[] gamma = new char[input[0].length()];
		for (int i = 0; i < gamma.length; i++) {
			final int index = i;
			long count = Stream.of(input)
					.mapToInt(line -> line.charAt(index))
					.filter(chr -> chr == '1')
					.count();
			char chr = count > input.length / 2 ? '1' : '0';
			gamma[index] = chr;
		}

		int gammaInt = Integer.parseInt(String.valueOf(gamma), 2);
		int epsilonInt = Integer.parseInt(
				String.valueOf(gamma)
						.replace('1', 'O')
						.replace('0', '1')
						.replace('O', '0'),
				2);
		return gammaInt * epsilonInt;
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(calculateLifeSupportRating(SAMPLE)).isEqualTo(230);
	}

	@Test
	void partTwoInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(calculateLifeSupportRating(lines.toArray(String[]::new))).isEqualTo(4375225);
	}

	private static int calculateLifeSupportRating(String[] input) {
		int co2ScrubberRating = co2ScrubberRating(input);
		int oxygenGeneratorRating = oxygenGeneratorRating(input);
		return co2ScrubberRating * oxygenGeneratorRating;
	}

	private static int oxygenGeneratorRating(String[] input) {
		List<String> list = new ArrayList<>(List.of(input));
		int i = 0;
		while (list.size() > 1) {
			final int index = i;
			list.removeIf(line -> mostCommonOrOne(line, list, index));
			i++;
		}
		return Integer.parseInt(list.get(0), 2);
	}

	private static boolean mostCommonOrOne(String line, List<String> list, int index) {
		Map<Character, Integer> countPerIndex = countPerIndex(list, index);
		char charAt = line.charAt(index);
		int zeroCount = countPerIndex.getOrDefault('0', 0);
		int oneCount = countPerIndex.getOrDefault('1', 0);
		return charAt == '1' ? zeroCount <= oneCount : zeroCount > oneCount;
	}

	private static int co2ScrubberRating(String[] input) {
		List<String> list = new ArrayList<>(List.of(input));
		int i = 0;
		while (list.size() > 1) {
			final int index = i;
			list.removeIf(line -> leastCommonOrZero(line, list, index));
			i++;
		}
		return Integer.parseInt(list.get(0), 2);
	}

	private static boolean leastCommonOrZero(String line, List<String> list, int index) {
		Map<Character, Integer> countPerIndex = countPerIndex(list, index);
		char charAt = line.charAt(index);
		int zeroCount = countPerIndex.getOrDefault('0', 0);
		int oneCount = countPerIndex.getOrDefault('1', 0);
		return charAt == '0' ? zeroCount <= oneCount : zeroCount > oneCount;
	}

	private static Map<Character, Integer> countPerIndex(List<String> input, int index) {
		return input.stream()
				.map(line -> line.charAt(index))
				.collect(groupingBy(identity()))
				.entrySet()
				.stream()
				.collect(toMap(
						Map.Entry::getKey,
						entry -> entry.getValue().size()));
	}

}