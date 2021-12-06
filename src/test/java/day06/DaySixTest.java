package day06;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toMap;
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
	void partTwoSample() throws Exception {
		assertThat(countNumberOfFishAfterNumberOfDays(SAMPLE, 256)).isEqualTo(26984457539L);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countNumberOfFishAfterNumberOfDays(input, 256)).isEqualTo(1605400130036L);
	}

	private static long countNumberOfFishAfterNumberOfDays(String input, int days) {
		return Stream.iterate(
				Stream.of(input.split(","))
						.map(Integer::valueOf)
						.collect(groupingBy(identity(), counting())),
				fish -> fish.entrySet().stream()
						.flatMap(entry -> entry.getKey() == 0
								? Stream.of(
										Map.entry(6, entry.getValue()),
										Map.entry(8, entry.getValue()))
								: Stream.of(Map.entry(entry.getKey() - 1, entry.getValue())))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum)))
				.skip(days)
				.findFirst()
				.map(map -> map.values().stream().collect(summingLong(Long::longValue)))
				.get();
	}

}
