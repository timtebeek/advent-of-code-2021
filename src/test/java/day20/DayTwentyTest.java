package day20;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class DayTwentyTest {

	@Test
	@Disabled
	void partOneSample() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		Image enhanced = Stream.iterate(Parser.parse(sample), Image::enhance)
				.skip(2).findFirst().get();
		assertThat(enhanced.pixels().values().stream().filter(i -> i == 1).count()).isEqualByComparingTo(35L);
	}

	@Test
	void partOneInput() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		Image enhanced = Stream.iterate(Parser.parse(sample), Image::enhance)
				.skip(2).findFirst().get();
		assertThat(enhanced.pixels().values().stream().filter(i -> i == 1).count()).isEqualByComparingTo(4968L);
	}

	@Test
	void partOneOther() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("other").toURI()));
		Image enhanced = Stream.iterate(Parser.parse(sample), Image::enhance)
				.skip(2).findFirst().get();
		assertThat(enhanced.pixels().values().stream().filter(i -> i == 1).count()).isEqualByComparingTo(5326L);
	}

	@Test
	void partTwoInput() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		Image enhanced = Stream.iterate(Parser.parse(sample), Image::enhance)
				.skip(50).findFirst().get();
		assertThat(enhanced.pixels().values().stream().filter(i -> i == 1).count()).isEqualByComparingTo(16793L);
	}

}

class Parser {

	public static Image parse(String input) {
		String[] split = input.split("\n\n");
		return new Image(
				split[0],
				parsePixels(split[1]),
				split[0].charAt(0) == '#' ? 0 : 1);
	}

	private static Map<Pixel, Integer> parsePixels(String imageBlock) {
		Map<Pixel, Integer> pixels = new HashMap<>();
		String[] lines = imageBlock.split("\n");
		for (int y = 0; y < lines.length; y++) {
			String[] split = lines[y].split("");
			for (int x = 0; x < split.length; x++) {
				pixels.put(new Pixel(x, y), split[x].equals("#") ? 1 : 0);
			}
		}
		return pixels;
	}

}

record Image(String enhancement, Map<Pixel, Integer> pixels, int defaultValue) {

	Image enhance() {
		Map<Pixel, Integer> nextPixels = allPixels().collect(toMap(Function.identity(), pixel -> {
			String bits = pixel.neighbors()
					.map(p -> "" + pixels.getOrDefault(p, defaultValue))
					.collect(joining());
			int index = Integer.parseInt(bits, 2);
			return enhancement.charAt(index) == '#' ? 1 : 0;
		}));
		return new Image(enhancement, nextPixels, 1 - defaultValue);
	}

	private Stream<Pixel> allPixels() {
		IntSummaryStatistics xstats = pixels.keySet().stream().mapToInt(Pixel::x).summaryStatistics();
		IntSummaryStatistics ystats = pixels.keySet().stream().mapToInt(Pixel::y).summaryStatistics();
		int padding = 1;
		return rangeClosed(ystats.getMin() - padding, ystats.getMax() + padding)
				.mapToObj(y -> rangeClosed(xstats.getMin() - padding, xstats.getMax() + padding)
						.mapToObj(x -> new Pixel(x, y)))
				.flatMap(Function.identity());
	}

	@Override
	public String toString() {
		List<Pixel> list = pixels.keySet().stream().sorted().toList();
		int y = Integer.MIN_VALUE;
		StringBuilder sb = new StringBuilder();
		for (Pixel point : list) {
			if (y < point.y()) {
				y = point.y();
				sb.append('\n');
			}
			sb.append(pixels.getOrDefault(point, defaultValue) == 1 ? '#' : '.');
		}
		return sb.toString();
	}

}

record Boundary(Pixel topLeft, Pixel bottomRight) {
	boolean within(Pixel pixel) {
		return topLeft.compareTo(pixel) <= 0 && pixel.compareTo(bottomRight) <= 0;
	}
}

record Pixel(int x, int y) implements Comparable<Pixel> {

	private static final Comparator<Pixel> COMPARATOR = comparing(Pixel::y).thenComparing(Pixel::x);

	public Stream<Pixel> neighbors() {
		return Stream.of(
				new Pixel(x - 1, y - 1), new Pixel(x, y - 1), new Pixel(x + 1, y - 1),
				new Pixel(x - 1, y), new Pixel(x, y), new Pixel(x + 1, y),
				new Pixel(x - 1, y + 1), new Pixel(x, y + 1), new Pixel(x + 1, y + 1));
	}

	@Override
	public int compareTo(Pixel that) {
		return COMPARATOR.compare(this, that);
	}

}
