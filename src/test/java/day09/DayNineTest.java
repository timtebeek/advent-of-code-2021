package day09;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
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
		return heightMap.lowPoints().stream()
				.mapToInt(point -> heightMap.heights().get(point) + 1) // Risk
				.sum();
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

	public Set<Point> lowPoints() {
		return flow.keySet().stream()
				.map(this::findLowPointFor)
				.collect(toSet());
	}

	private Point findLowPointFor(Point point) {
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