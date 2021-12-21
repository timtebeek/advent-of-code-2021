package day20;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
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
		assertThat(Stream.iterate(Parser.parse("sample"), Image::enhance)
				.skip(2).findFirst().get().lit()).isEqualByComparingTo(35L);
	}

	@Test
	void partOneInput() throws Exception {
		assertThat(Stream.iterate(Parser.parse("input"), Image::enhance)
				.skip(2).findFirst().get().lit()).isEqualByComparingTo(4968L);
	}

	@Test
	void partOneOther() throws Exception {
		assertThat(Stream.iterate(Parser.parse("other"), Image::enhance)
				.skip(2).findFirst().get().lit()).isEqualByComparingTo(5326L);
	}

	@Test
	void partTwoInput() throws Exception {
		assertThat(Stream.iterate(Parser.parse("input"), Image::enhance)
				.peek(System.out::println)
				.skip(50).findFirst().get().lit()).isEqualByComparingTo(16793L);
	}

}

class Parser {

	public static Image parse(String filename) throws Exception {
		String[] split = Files.readString(Paths.get(Parser.class.getResource(filename).toURI())).split("\n\n");
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
		return new Image(
				enhancement,
				nextPixels().collect(toMap(Function.identity(), this::nextValue)),
				1 - defaultValue);
	}

	private Stream<Pixel> nextPixels() {
		var xstats = pixels.keySet().stream().mapToInt(Pixel::x).summaryStatistics();
		var ystats = pixels.keySet().stream().mapToInt(Pixel::y).summaryStatistics();
		return rangeClosed(ystats.getMin() - 1, ystats.getMax() + 1)
				.mapToObj(y -> rangeClosed(xstats.getMin() - 1, xstats.getMax() + 1)
						.mapToObj(x -> new Pixel(x, y)))
				.flatMap(Function.identity());
	}

	private Integer nextValue(Pixel pixel) {
		String bits = pixel.neighbors()
				.map(p -> "" + pixels.getOrDefault(p, defaultValue))
				.collect(joining());
		return enhancement.charAt(Integer.parseInt(bits, 2)) == '#' ? 1 : 0;
	}

	@Override
	public String toString() {
		int y = Integer.MIN_VALUE;
		StringBuilder sb = new StringBuilder();
		for (Pixel point : pixels.keySet().stream().sorted().toList()) {
			if (y < point.y()) {
				y = point.y();
				sb.append('\n');
			}
			sb.append(pixels.get(point) == 1 ? '#' : '.');
		}
		return sb.toString();
	}

	public long lit() {
		return pixels.values().stream().filter(i -> i == 1).count();
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
