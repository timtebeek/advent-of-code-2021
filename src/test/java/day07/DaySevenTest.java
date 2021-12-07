package day07;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class DaySevenTest {

	private static final String SAMPLE = "16,1,2,0,4,2,7,1,2,14";

	@Test
	void partOneSample() throws Exception {
		assertThat(countLeastAmountOfFuelForPartOne(SAMPLE)).isEqualTo(37);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countLeastAmountOfFuelForPartOne(input)).isEqualTo(342730);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(countLeastAmountOfFuelForPartTwo(SAMPLE)).isEqualTo(168);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countLeastAmountOfFuelForPartTwo(input)).isEqualTo(92335207);
	}

	private static long countLeastAmountOfFuelForPartOne(String input) {
		int[] crabs = Stream.of(input.split(",")).mapToInt(Integer::parseInt).toArray();
		return rangeClosed(
				IntStream.of(crabs).min().getAsInt(),
				IntStream.of(crabs).max().getAsInt())
						.map(pos -> IntStream.of(crabs).map(crab -> Math.abs(pos - crab)).sum())
						.min().getAsInt();
	}

	private static long countLeastAmountOfFuelForPartTwo(String input) {
		int[] crabs = Stream.of(input.split(",")).mapToInt(Integer::parseInt).toArray();
		return rangeClosed(
				IntStream.of(crabs).min().getAsInt(),
				IntStream.of(crabs).max().getAsInt())
						.map(pos -> IntStream.of(crabs).map(crab -> rangeClosed(1, Math.abs(pos - crab)).sum()).sum())
						.min().getAsInt();
	}

}
