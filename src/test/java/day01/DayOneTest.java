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
		assertThat(countIncreasesPerOffset(Stream.of(SAMPLE), 1)).isEqualTo(7);
	}

	@Test
	void partOneInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countIncreasesPerOffset(lines, 1)).isEqualTo(1184);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(countIncreasesPerOffset(Stream.of(SAMPLE), 3)).isEqualTo(5);
	}

	@Test
	void partTwoInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countIncreasesPerOffset(lines, 3)).isEqualTo(1158);
	}

	private static int countIncreasesPerOffset(Stream<String> strings, int offset) {
		int[] ints = strings
				.mapToInt(Integer::parseInt)
				.toArray();
		return IntStream
				.range(0, ints.length - offset)
				.map(i -> ints[i] < ints[i + offset] ? 1 : 0)
				.sum();
	}

}
