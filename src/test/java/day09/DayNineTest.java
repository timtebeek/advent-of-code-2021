package day09;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class DayNineTest {

	private static final String SAMPLE = """
			2199943210
			3987894921
			9856789892
			8767896789
			9899965678
			""";

	@Test
	void partOneSample() throws Exception {
		assertThat(countSumOfRisk(SAMPLE)).isEqualTo(15);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(countSumOfRisk(input)).isEqualTo(535);
	}

	private static int countSumOfRisk(String sample) {
		HeightMap heightMap = HeightMap.parse(sample);
		return heightMap.flow().keySet().stream()
				.map(heightMap::findLowPointFor)
				.distinct()
				.mapToInt(point -> heightMap.heights().get(point) + 1) // Risk
				.sum();
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(multiplyThreeLargestBasins(SAMPLE)).isEqualTo(1134);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(multiplyThreeLargestBasins(input)).isEqualTo(1122700);
	}

	private static Long multiplyThreeLargestBasins(String sample) {
		HeightMap heightMap = HeightMap.parse(sample);
		Map<Point, Long> countPerLowPoint = heightMap.heights().keySet().stream()
				.filter(point -> heightMap.heights().get(point) != 9)
				.collect(groupingBy(point -> heightMap.findLowPointFor(point), counting()));
		return countPerLowPoint.values().stream()
				.sorted((a, b) -> b.compareTo(a))
				.limit(3)
				.reduce((a, b) -> a * b)
				.get();
	}

}

record HeightMap(
		Map<Point, Integer> heights,
		Map<Point, Point> flow) {

	static HeightMap parse(String input) {
		Map<Point, Integer> points = parseHeights(input);
		Map<Point, Point> flow = flow(points);
		return new HeightMap(points, flow);
	}

	Point findLowPointFor(Point point) {
		Point flowsTo = flow.get(point);
		if (flowsTo.equals(point)) {
			return point;
		}
		return findLowPointFor(flowsTo);
	}

	private static Map<Point, Point> flow(Map<Point, Integer> heights) {
		return heights.entrySet().stream().collect(toMap(
				Map.Entry::getKey,
				point -> findLowPoint(point, heights)));
	}

	private static Point findLowPoint(Map.Entry<Point, Integer> point, Map<Point, Integer> heights) {
		Point lowest = point.getKey();
		for (Point other : List.of(
				new Point(lowest.row() - 1, lowest.column()), // up
				new Point(lowest.row() + 1, lowest.column()), // down
				new Point(lowest.row(), lowest.column() - 1), // left
				new Point(lowest.row(), lowest.column() + 1))) {// right
			Integer currentHeight = heights.get(lowest);
			Integer otherHeight = heights.getOrDefault(other, currentHeight + 1);
			if (otherHeight <= currentHeight) {
				lowest = other;
			}
		}
		return lowest;
	}

	private static Map<Point, Integer> parseHeights(String input) {
		String[] split = input.split("\n");
		var points = new HashMap<Point, Integer>();
		for (int row = 0; row < split.length; row++) {
			String[] heights = split[row].split("");
			for (int coumns = 0; coumns < heights.length; coumns++) {
				points.put(new Point(row, coumns), Integer.valueOf(heights[coumns]));
			}
		}
		return points;
	}

}

record Point(int row, int column) {
}