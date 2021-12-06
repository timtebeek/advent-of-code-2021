package day06;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DaySixTest {

	private static final String SAMPLE = "3,4,3,1,2";

	@Test
	void partOneSample() throws Exception {
		assertThat(countNumberOfFishAfterNumberOfDays(SAMPLE, 80)).isEqualTo(5934);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countNumberOfFishAfterNumberOfDays(input, 80)).isEqualTo(353079);
	}

	@Test
	@Disabled
	void partTwoSample() throws Exception {
		assertThat(countNumberOfFishAfterNumberOfDays(SAMPLE, 256)).isEqualTo(26984457539L);
	}

	@Test
	@Disabled
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countNumberOfFishAfterNumberOfDays(input, 256)).isEqualTo(-1);
	}

	private static int countNumberOfFishAfterNumberOfDays(String input, int days) {
		return Stream.iterate(
				Stream.of(input.split(","))
						.map(Integer::valueOf)
						.toList(),
				fish -> fish.stream()
						.flatMap(p -> p == 0 ? Stream.of(6, 8) : Stream.of(--p))
						.toList())
				.limit(days + 1)
				.mapToInt(List::size)
				.reduce((a, b) -> b)
				.getAsInt();
	}

}
