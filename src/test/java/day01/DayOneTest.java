package day01;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayOneTest {

	private static final String[] SAMPLE = """
			199
			200
			208
			210
			200
			207
			240
			269
			260
			263""".split("\n");

	@Test
	void partOneSample() throws Exception {
		Stream<String> lines = Stream.of(SAMPLE);
		assertThat(countIncreasesPerPair(lines)).isEqualTo(7);
	}

	@Test
	void partOneInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countIncreasesPerPair(lines)).isEqualTo(1184);
	}

	@Test
	void partTwoSample() throws Exception {
		Stream<String> lines = Stream.of(SAMPLE);
		assertThat(countIncreasesPerWindow(lines)).isEqualTo(5);
	}

	@Test
	void partTwoInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countIncreasesPerWindow(lines)).isEqualTo(1158);
	}

	private static int countIncreasesPerPair(Stream<String> strings) {
		int[] ints = strings
				.mapToInt(Integer::parseInt)
				.toArray();
		return IntStream
				.range(0, ints.length - 1)
				.map(i -> ints[i] < ints[i + 1] ? 1 : 0)
				.sum();
	}

	private static int countIncreasesPerWindow(Stream<String> strings) {
		int[] ints = strings
				.mapToInt(Integer::parseInt)
				.toArray();
		return IntStream
				.range(0, ints.length - 3)
				.map(i -> ints[i] + ints[i + 1] + ints[i + 2] < ints[i + 1] + ints[i + 2] + ints[i + 3] ? 1 : 0)
				.sum();
	}

}
