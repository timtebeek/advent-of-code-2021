package day20;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class DayTwentyTest {

	@Test
	void partOneSample() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		Image twiceEnhanced = Stream.iterate(Parser.parse(sample), Image::enhance)
				.peek(System.out::println)
				.skip(2)
				.findFirst().get();
		assertThat(twiceEnhanced.pixels().size()).isEqualByComparingTo(35);
	}

	@Test
	@Disabled
	void partOneInput() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		Image twiceEnhanced = Stream.iterate(Parser.parse(sample), Image::enhance)
				.peek(System.out::println)
				.skip(2)
				.findFirst().get();
		int size = twiceEnhanced.pixels().size();
		System.out.println(size);
		assertThat(size).isEqualByComparingTo(-1);
	}

	@Test
	void partOneOther() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("other").toURI()));
		Image twiceEnhanced = Stream.iterate(Parser.parse(sample), Image::enhance)
				.peek(System.out::println)
				.skip(2)
				.findFirst().get();
		assertThat(twiceEnhanced.pixels().size()).isEqualByComparingTo(5326);
	}

}

class Parser {

	public static Image parse(String input) {
		String[] split = input.split("\n\n");
		return new Image(split[0], parsePixels(split[1]), split[0].charAt(0) == '#' ? "1" : "0");
	}

	private static Set<Pixel> parsePixels(String imageBlock) {
		Set<Pixel> pixels = new HashSet<>();
		String[] lines = imageBlock.split("\n");
		for (int y = 0; y < lines.length; y++) {
			String[] split = lines[y].split("");
			for (int x = 0; x < split.length; x++) {
				if (split[x].equals("#")) {
					pixels.add(new Pixel(x, y));
				}
			}
		}
		return pixels;
	}

}

record Image(String enhancement, Set<Pixel> pixels, String defaultValue) {

	Image enhance() {
		Boundary boundary = boundary();
		Set<Pixel> nextPixels = allPixels(boundary).flatMap(pixel -> {
			String bits = pixel.neighbors()
					.map(p -> pixels.contains(p) ? "1" : defaultValue)
					.collect(joining());
			int index = Integer.parseInt(bits, 2);
			return enhancement.charAt(index) == '#' ? Stream.of(pixel) : Stream.empty();
		}).collect(toSet());
		return new Image(enhancement, nextPixels, defaultValue); // TODO Flip default?
	}

	private static Stream<Pixel> allPixels(Boundary boundary) {
		int padding = 1;
		return IntStream.rangeClosed(
				boundary.topLeft().y() - padding,
				boundary.bottomRight().y() + padding)
				.mapToObj(y -> IntStream
						.rangeClosed(
								boundary.topLeft().x() - padding,
								boundary.bottomRight().x() + padding)
						.mapToObj(x -> new Pixel(x, y)))
				.flatMap(Function.identity());
	}

	private Boundary boundary() {
		IntSummaryStatistics statisticsX = pixels.stream().mapToInt(Pixel::x).summaryStatistics();

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (Pixel point : pixels) {
			if (point.x() < minX) {
				minX = point.x();
			}
			if (point.y() < minY) {
				minY = point.y();
			}
			if (maxX < point.x()) {
				maxX = point.x();
			}
			if (maxY < point.y()) {
				maxY = point.y();
			}
		}
		return new Boundary(
				new Pixel(minX, minY),
				new Pixel(maxX, maxY));
	}

	@Override
	public String toString() {
		List<Pixel> list = allPixels(boundary()).toList();
		int y = Integer.MIN_VALUE;
		StringBuilder sb = new StringBuilder();
		for (Pixel point : list) {
			if (y < point.y()) {
				y = point.y();
				sb.append('\n');
			}
			sb.append(pixels.contains(point) ? '#' : '.');
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

	private static final Comparator<Pixel> COMPARATOR = comparing(Pixel::x).thenComparing(Pixel::y);

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
