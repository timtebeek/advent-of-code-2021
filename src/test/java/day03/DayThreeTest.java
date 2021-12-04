package day03;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
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
		assertThat(calculatePowerConsumption(List.of(SAMPLE))).isEqualTo(198);
	}

	@Test
	void partOneInput() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(calculatePowerConsumption(lines)).isEqualTo(3885894);
	}

	private static int calculatePowerConsumption(List<String> input) {
		char[] gamma = new char[input.get(0).length()];
		char[] epsilon = new char[input.get(0).length()];
		for (int i = 0; i < gamma.length; i++) {
			List<Character> list = countPerIndex(input, i)
					.entrySet()
					.stream()
					.sorted(Comparator.comparing(Map.Entry::getValue))
					.map(Map.Entry::getKey)
					.toList();
			epsilon[i] = list.get(0);
			gamma[i] = list.get(1);
		}
		return Integer.parseInt(String.valueOf(gamma), 2)
				* Integer.parseInt(String.valueOf(epsilon), 2);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(calculateLifeSupportRating(List.of(SAMPLE))).isEqualTo(230);
	}

	@Test
	void partTwoInput() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(calculateLifeSupportRating(lines)).isEqualTo(4375225);
	}

	private static int calculateLifeSupportRating(List<String> input) {
		return generatorRating(input, '0')
				* generatorRating(input, '1');
	}

	private static int generatorRating(List<String> input, char whenEqualRetain) {
		List<String> list = new ArrayList<>(input);
		int i = 0;
		while (list.size() > 1) {
			final int index = i;
			list.removeIf(line -> compareAndCount(line, list, index, whenEqualRetain));
			i++;
		}
		return Integer.parseInt(list.get(0), 2);
	}

	private static boolean compareAndCount(String line, List<String> list, int index, char whenEqualRetain) {
		Map<Character, Long> countPerIndex = countPerIndex(list, index);
		char charAt = line.charAt(index);
		long zeroCount = countPerIndex.getOrDefault('0', 0L);
		long oneCount = countPerIndex.getOrDefault('1', 0L);
		return charAt == whenEqualRetain ? zeroCount <= oneCount : zeroCount > oneCount;
	}

	private static Map<Character, Long> countPerIndex(List<String> input, int index) {
		return input.stream()
				.map(line -> line.charAt(index))
				.collect(groupingBy(identity(), counting()));
	}

}