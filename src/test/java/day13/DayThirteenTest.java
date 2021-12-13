package day13;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static day13.Direction.LEFT;
import static day13.Direction.UP;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class DayThirteenTest {

	private static final String SAMPLE = """
			6,10
			0,14
			9,10
			0,3
			10,4
			4,11
			6,0
			6,12
			4,1
			0,13
			10,12
			3,4
			3,0
			8,4
			1,10
			2,14
			8,10
			9,0

			fold along y=7
			fold along x=5
			""";

	@Test
	void partOneSample() throws Exception {
		Paper paper = Paper.parse(SAMPLE);
		assertThat(paper.fold(paper.instructions().get(0)).dots().size()).isEqualTo(17);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		Paper paper = Paper.parse(input);
		List<Instruction> instructions = paper.instructions();
		for (Instruction instruction : instructions) {
			paper = paper.fold(instruction);
		}
		assertThat(paper.toString()).isEqualTo("""
				####..##...##..#..#.###..####.#..#.####
				#....#..#.#..#.#..#.#..#....#.#..#.#...
				###..#..#.#....#..#.#..#...#..####.###.
				#....####.#.##.#..#.###...#...#..#.#...
				#....#..#.#..#.#..#.#.#..#....#..#.#...
				#....#..#..###..##..#..#.####.#..#.####""");
	}

}

record Paper(Set<Point> dots, List<Instruction> instructions) {

	public static Paper parse(String sample) {
		String[] split = sample.split("\n\n");
		Set<Point> dots = Stream.of(split[0].split("\n"))
				.map(line -> line.split(","))
				.map(xy -> new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])))
				.collect(toSet());
		List<Instruction> instructions = Stream.of(split[1].split("\n"))
				.map(line -> line.split(" ")[2])
				.map(instr -> instr.split("="))
				.map(xynumber -> new Instruction(xynumber[0].equals("x") ? LEFT : UP, Integer.parseInt(xynumber[1])))
				.toList();
		return new Paper(dots, instructions);
	}

	public Paper fold(Instruction instruction) {
		return new Paper(dots.stream()
				.map(p -> foldPoint(p, instruction))
				.collect(toSet()),
				instructions);
	}

	private static Point foldPoint(Point p, Instruction instruction) {
		Direction direction = instruction.direction();
		int line = instruction.line();
		return direction == UP
				? new Point(p.x(), p.y() < line ? p.y() : line - (p.y() - line))
				: new Point(p.x() < line ? p.x() : line - (p.x() - line), p.y());
	}

	@Override
	public String toString() {
		int maxx = dots.stream().mapToInt(Point::x).max().getAsInt();
		int maxy = dots.stream().mapToInt(Point::y).max().getAsInt();
		return IntStream.rangeClosed(0, maxy)
				.mapToObj(y -> drawLine(maxx, y))
				.collect(joining("\n"));
	}

	private String drawLine(int maxx, int y) {
		return IntStream.rangeClosed(0, maxx)
				.mapToObj(x -> dots.contains(new Point(x, y)) ? "#" : ".")
				.collect(joining());
	}

}

record Point(int x, int y) {
}

record Instruction(Direction direction, int line) {
}

enum Direction {
	LEFT, UP
}