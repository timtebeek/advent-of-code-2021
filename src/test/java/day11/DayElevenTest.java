package day11;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayElevenTest {

	private static final String SAMPLE = """
			5483143223
			2745854711
			5264556173
			6141336146
			6357385478
			4167524645
			2176841721
			6882881134
			4846848554
			5283751526
			""";

	@Test
	void partOneSample() throws Exception {
		assertThat(countFlashesAfterIterations(SAMPLE, 100)).isEqualTo(1656);
	}

	private static final String INPUT = """
			4781623888
			1784156114
			3265645122
			4371551414
			3377154886
			7882314455
			6421348681
			7175424287
			5488242184
			2448568261
			""";

	@Test
	void partOneInput() throws Exception {
		assertThat(countFlashesAfterIterations(INPUT, 100)).isEqualTo(1713);
	}

	private static int countFlashesAfterIterations(String sample, int iterations) {
		return Stream.iterate(
				Grid.parse(sample),
				Grid::step)
				.skip(iterations)
				.mapToInt(Grid::flashes)
				.findFirst()
				.getAsInt();
	}
}

record Grid(Map<Point, Integer> octopuses, int flashes) {

	public static Grid parse(String input) {
		String[] split = input.split("\n");
		var points = new HashMap<Point, Integer>();
		for (int row = 0; row < split.length; row++) {
			String[] values = split[row].split("");
			for (int coumns = 0; coumns < values.length; coumns++) {
				points.put(new Point(row, coumns), Integer.valueOf(values[coumns]));
			}
		}
		return new Grid(points, 0);
	}

	public Grid step() {
		Map<Point, Integer> nextOctopuses = new HashMap<>(octopuses);
		nextOctopuses.replaceAll((t, u) -> u + 1);
		Set<Point> hasFlashed = new HashSet<>();

		int flashCount;
		do {

			flashCount = hasFlashed.size();

			// Cascading flashes this step
			List<Point> flashing = nextOctopuses.entrySet().stream()
					.filter(entry -> !hasFlashed.contains(entry.getKey()))
					.filter(entry -> 9 < entry.getValue())
					.map(Entry::getKey)
					.toList();
			hasFlashed.addAll(flashing);

			// Increase neighbours
			flashing.stream()
					.flatMap(Point::neighbours)
					.forEach(neighbour -> nextOctopuses.compute(neighbour, (t, u) -> u + 1));

		} while (flashCount < hasFlashed.size()); // Continue until there's no more increase

		// Set to zero
		nextOctopuses.replaceAll((t, u) -> 9 < u ? 0 : u);

		return new Grid(nextOctopuses, flashes + hasFlashed.size());
	}
}

record Point(int row, int column) {

	static Stream<Point> neighbours(Point c) {
		return Stream.of(
				new Point(c.row() - 1, c.column() - 1),
				new Point(c.row() - 1, c.column()),
				new Point(c.row() - 1, c.column() + 1),

				new Point(c.row(), c.column() - 1),
				// Centre
				new Point(c.row(), c.column() + 1),

				new Point(c.row() + 1, c.column() - 1),
				new Point(c.row() + 1, c.column()),
				new Point(c.row() + 1, c.column() + 1))
				.filter(p -> 0 <= p.row() && p.row() < 10
						&& 0 <= p.column() && p.column() < 10);
	}

}
