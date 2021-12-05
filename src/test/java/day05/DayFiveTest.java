package day05;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

class DayFiveTest {

	private static final String SAMPLE = """
			0,9 -> 5,9
			8,0 -> 0,8
			9,4 -> 3,4
			2,2 -> 2,1
			7,0 -> 7,4
			6,4 -> 2,0
			0,9 -> 2,9
			3,4 -> 1,4
			0,0 -> 8,8
			5,5 -> 8,2
			""";

	@Test
	void partOneSample() throws Exception {
		assertThat(FloorMap.parse(SAMPLE, false).countOverlappingPoints()).isEqualTo(5);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(FloorMap.parse(input, false).countOverlappingPoints()).isEqualTo(5294);
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(FloorMap.parse(SAMPLE, true).countOverlappingPoints()).isEqualTo(12);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(FloorMap.parse(input, true).countOverlappingPoints()).isEqualTo(21698);
	}

}

record FloorMap(Map<Point, Long> points) {

	static FloorMap parse(String sample, boolean includeDiagonal) {
		return new FloorMap(Stream.of(sample.split("\n"))
				.map(Line::of)
				.filter(line -> line.isHorizontal() || line.isVertical() || includeDiagonal)
				.flatMap(line -> line.points().stream())
				.collect(groupingBy(
						Function.identity(),
						Collectors.counting())));
	}

	long countOverlappingPoints() {
		return points.values().stream().filter(l -> 1 < l).count();
	}

}

record Line(Point from, Point to) {

	static Line of(String line) {
		String[] split = line.split(" -> ");
		Point a = Point.of(split[0]);
		Point b = Point.of(split[1]);
		return a.compareTo(b) < 0 ? new Line(a, b) : new Line(b, a);
	}

	boolean isHorizontal() {
		return from.x() == to.x();
	}

	boolean isVertical() {
		return from.y() == to.y();
	}

	List<Point> points() {
		if (isHorizontal()) {
			return IntStream.rangeClosed(from.y(), to.y())
					.mapToObj(y -> new Point(from.x(), y))
					.toList();
		}
		if (isVertical()) {
			return IntStream.rangeClosed(from.x(), to.x())
					.mapToObj(x -> new Point(x, from.y()))
					.toList();
		}
		List<Point> diagonal = new ArrayList<>();
		int x = from.x();
		int y = from.y();
		int xStep = from.x() < to.x() ? 1 : -1;
		int yStep = from.y() < to.y() ? 1 : -1;
		int steps = Math.abs(from.x() - to.x());
		for (int step = 0; step <= steps; step++) {
			diagonal.add(new Point(x, y));
			x += xStep;
			y += yStep;
		}
		return diagonal;
	}

}

record Point(int x, int y) implements Comparable<Point> {

	static Point of(String coordinate) {
		String[] split = coordinate.split(",");
		return new Point(
				Integer.parseInt(split[0]),
				Integer.parseInt(split[1]));
	}

	@Override
	public int compareTo(Point o) {
		return comparing(Point::x)
				.thenComparing(Point::y)
				.compare(this, o);
	}

}
