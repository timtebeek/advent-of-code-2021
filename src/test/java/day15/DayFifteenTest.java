package day15;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class DayFifteenTest {

	private static final String SAMPLE = """
			1163751742
			1381373672
			2136511328
			3694931569
			7463417111
			1319128137
			1359912421
			3125421639
			1293138521
			2311944581
			""";

	@Test
	void partOneSample() {
		assertThat(lowestTotalRisk(SAMPLE)).isEqualTo(40);
	}

	@Test
//	@Disabled
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(lowestTotalRisk(input)).isEqualTo(-1);
	}

	private static long lowestTotalRisk(String input) {
		Cave cave = Cave.parse(input);
		List<Point> path = new ArrayList<>(200);
		Point start = new Point(0, 0);
		path.add(start);
		return cave.lowestTotalRiskPath(Long.MAX_VALUE, path) - cave.risk().get(start);
	}

}

record Cave(Map<Point, Long> risk, Map<Point, List<Point>> connections, Point end) {

	static Cave parse(String input) {
		// Risk map
		Map<Point, Long> riskMap = new HashMap<>();
		String[] lines = input.split("\n");
		for (int y = 0; y < lines.length; y++) {
			String[] positions = lines[y].split("");
			for (int x = 0; x < positions.length; x++) {
				Long risk = Long.valueOf(positions[x]);
				riskMap.put(new Point(x, y), risk);
			}
		}

		// End point
		int maxX = riskMap.keySet().stream().mapToInt(Point::x).max().getAsInt();
		int maxY = riskMap.keySet().stream().mapToInt(Point::y).max().getAsInt();
		Point end = new Point(maxX, maxY);

		// Connections between points
		Map<Point, List<Point>> connections = riskMap.keySet().stream()
				.collect(toMap(Function.identity(), p -> p.connections(end)
						// .sorted((a, b) -> Long.compare(riskMap.get(a), riskMap.get(b)))
						.toList()));

		return new Cave(riskMap, connections, end);
	}

	long lowestTotalRiskPath(long lowestSoFar, List<Point> path) {
		Point tail = path.get(path.size() - 1);
		long score = path.stream().mapToLong(risk::get).sum();		// TODO Use lowest soFar instead
		// Do not explore further
		if (tail.equals(end)) {
			return score;
		}

		List<Point> list = connections.get(tail).stream()
				.filter(Predicate.not(path::contains))
				.toList();
		if (list.isEmpty() || lowestSoFar < score) {
			return Long.MAX_VALUE;
		}

		// Explore each option, limited by lowest so far
		long lowestThisRound = lowestSoFar;
		for (Point point : list) {
			path.add(point);
			long lowestFromPath = lowestTotalRiskPath(lowestThisRound, path);
			if (lowestFromPath < lowestThisRound) {
				lowestThisRound = lowestFromPath;
				System.out.println("%d %s".formatted(lowestFromPath, path));
			}
			path.remove(path.size() - 1);
		}
		return lowestThisRound;
	}

}

record Point(int x, int y) {
	Stream<Point> connections(Point max) {
		return Stream.of(
				new Point(x + 1, y),
				new Point(x, y + 1),
				new Point(x - 1, y),
				new Point(x, y - 1))
				.filter(p -> 0 <= p.x() && p.x() <= max.x()
						&& 0 <= p.y() && p.y() <= max.y());
	}
}
