package day07;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class DaySevenTest {

	private static final String SAMPLE = "16,1,2,0,4,2,7,1,2,14";

	private BiFunction<Integer, Integer, Integer> partOne = (crab, pos) -> Math.abs(pos - crab);
	private BiFunction<Integer, Integer, Integer> partTwo = (crab, pos) -> rangeClosed(1, Math.abs(pos - crab)).sum();

	@Test
	void partOneSample() throws Exception {
		assertThat(countLeastAmountOfFuelForAlignment(SAMPLE, partOne)).isEqualTo(37);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countLeastAmountOfFuelForAlignment(input, partOne)).isEqualTo(342730);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(countLeastAmountOfFuelForAlignment(SAMPLE, partTwo)).isEqualTo(168);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countLeastAmountOfFuelForAlignment(input, partTwo)).isEqualTo(92335207);
	}

	private static long countLeastAmountOfFuelForAlignment(String input, BiFunction<Integer, Integer, Integer> cost) {
		List<Integer> crabs = Stream.of(input.split(",")).map(Integer::valueOf).toList();
		return rangeClosed(
				crabs.stream().min(Integer::compareTo).get(),
				crabs.stream().max(Integer::compareTo).get())
						.map(pos -> crabs.stream().mapToInt(crab -> cost.apply(crab, pos)).sum())
						.min().getAsInt();
	}

}
