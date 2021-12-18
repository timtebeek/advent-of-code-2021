package day15;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
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
		assertThat(Cave.parse(SAMPLE).lowestTotalRiskScore()).isEqualTo(40);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(Cave.parse(input).lowestTotalRiskScore()).isEqualTo(540);
	}

	@Test
	void partTwoSample() {
		assertThat(Cave.parse(SAMPLE).multiply().lowestTotalRiskScore()).isEqualTo(315);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(Cave.parse(input).multiply().lowestTotalRiskScore()).isEqualTo(2879);
	}

}

record Cave(Map<Point, Long> risk, Point end) {

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
		long maxX = riskMap.keySet().stream().mapToLong(Point::x).max().getAsLong();
		long maxY = riskMap.keySet().stream().mapToLong(Point::y).max().getAsLong();
		Point end = new Point(maxX, maxY);

		return new Cave(riskMap, end);
	}

	Cave multiply() {
		Point shiftRight = new Point(end.x() + 1, 0);
		Map<Point, Long> once = incrementAndShift(risk, shiftRight);
		Map<Point, Long> twice = incrementAndShift(once, shiftRight);
		Map<Point, Long> three = incrementAndShift(twice, shiftRight);
		Map<Point, Long> four = incrementAndShift(three, shiftRight);

		Map<Point, Long> topRow = new HashMap<>(risk);
		topRow.putAll(once);
		topRow.putAll(twice);
		topRow.putAll(three);
		topRow.putAll(four);

		Point shiftDown = new Point(0, end.y() + 1);
		Map<Point, Long> secondRow = incrementAndShift(topRow, shiftDown);
		Map<Point, Long> thirdRow = incrementAndShift(secondRow, shiftDown);
		Map<Point, Long> fourthRow = incrementAndShift(thirdRow, shiftDown);
		Map<Point, Long> fifthRow = incrementAndShift(fourthRow, shiftDown);

		Map<Point, Long> finalCave = new HashMap<>(topRow);
		finalCave.putAll(secondRow);
		finalCave.putAll(thirdRow);
		finalCave.putAll(fourthRow);
		finalCave.putAll(fifthRow);

		long maxX = finalCave.keySet().stream().mapToLong(Point::x).max().getAsLong();
		long maxY = finalCave.keySet().stream().mapToLong(Point::y).max().getAsLong();
		return new Cave(finalCave, new Point(maxX, maxY));
	}

	private static Map<Point, Long> incrementAndShift(Map<Point, Long> risk, Point shift) {
		return risk.entrySet().stream()
				.map(e -> Map.entry(
						new Point(e.getKey().x() + shift.x(), e.getKey().y() + shift.y()),
						e.getValue() == 9 ? 1 : e.getValue() + 1))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	long lowestTotalRiskScore() {
		// h is the heuristic function. h(n) estimates the cost to reach goal from node n.
		Function<Point, Long> heuristicEstimateToReachGoal = p -> 0L; // Guaranteed shortest path using Dijkstra
		Point start = new Point(0, 0);
		Collection<Point> path = aStarPath(start, end, heuristicEstimateToReachGoal, risk::get);
		return path.stream()
				.mapToLong(risk::get)
				.sum() - risk.get(start);
	}

	/**
	 * @see https://en.wikipedia.org/wiki/A*_search_algorithm#Pseudocode
	 */
	private static Collection<Point> aStarPath(Point start, Point goal, Function<Point, Long> heuristic,
			Function<Point, Long> weight) {
		// For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from start to n currently
		// known.
		Map<Point, Point> cameFrom = new HashMap<>();

		// For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
		Map<Point, Long> gScore = new HashMap<>();
		gScore.put(start, 0L);

		// For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
		// how short a path from start to finish can be if it goes through n.
		Map<Point, Long> fScore = new HashMap<>();
		fScore.put(start, heuristic.apply(start));

		// Nodes to explore, sorted by their fScore
		SortedSet<Point> openSet = new TreeSet<>(Comparator
				.comparing((Point p) -> fScore.get(p))
				.thenComparing(p -> goal.x() - p.x())
				.thenComparing(p -> goal.y() - p.y()));
		openSet.add(start);

		while (!openSet.isEmpty()) {
			// This operation can occur in O(1) time if openSet is a min-heap or a priority queue
			// current := the node in openSet having the lowest fScore[] value
			Point current = openSet.first();
			if (current.equals(goal)) {
				return reconstruct(cameFrom, current);
			}
			openSet.remove(current);
			Long currentGScore = gScore.get(current);
			for (Point neighbor : current.connections(goal)) {
				// d(current,neighbor) is the weight of the edge from current to neighbor
				// tentative_gScore is the distance from start to the neighbor through current
				Long tentativeGScore = currentGScore + weight.apply(neighbor);
				if (tentativeGScore < gScore.getOrDefault(neighbor, Long.MAX_VALUE)) {
					// This path to neighbor is better than any previous one. Record it!
					cameFrom.put(neighbor, current);
					gScore.put(neighbor, tentativeGScore);
					fScore.put(neighbor, tentativeGScore + heuristic.apply(neighbor));
					openSet.add(neighbor);
				}
			}
		}

		throw new IllegalStateException("No route from %s to %s".formatted(start, goal));
	}

	private static Collection<Point> reconstruct(Map<Point, Point> cameFrom, Point current) {
		Deque<Point> path = new LinkedList<>();
		do {
			path.push(current);
			current = cameFrom.get(current);
		} while (current != null);
		return path;
	}
}

record Point(long x, long y) {
	List<Point> connections(Point max) {
		return Stream.of(
				new Point(x + 1, y),
				new Point(x, y + 1),
				new Point(x - 1, y),
				new Point(x, y - 1))
				.filter(p -> 0 <= p.x() && p.x() <= max.x()
						&& 0 <= p.y() && p.y() <= max.y())
				.toList();
	}
}
