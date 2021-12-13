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
		for (Instruction instruction : paper.instructions()) {
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
		return new Paper(
				Stream.of(split[0].split("\n")).map(Point::parse).collect(toSet()),
				Stream.of(split[1].split("\n")).map(Instruction::parse).toList());
	}

	public Paper fold(Instruction instruction) {
		return new Paper(dots.stream().map(p -> p.foldAlong(instruction)).collect(toSet()), instructions);
	}

	@Override
	public String toString() {
		int maxx = dots.stream().mapToInt(Point::x).max().getAsInt();
		int maxy = dots.stream().mapToInt(Point::y).max().getAsInt();
		return IntStream.rangeClosed(0, maxy)
				.mapToObj(y -> IntStream.rangeClosed(0, maxx)
						.mapToObj(x1 -> dots.contains(new Point(x1, y)) ? "#" : ".")
						.collect(joining()))
				.collect(joining("\n"));
	}

}

record Point(int x, int y) {
	static Point parse(String line) {
		String[] xy = line.split(",");
		return new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
	}

	Point foldAlong(Instruction instruction) {
		return new Point(
				x < instruction.line() || instruction.direction() == UP ? x : 2 * instruction.line() - x,
				y < instruction.line() || instruction.direction() == LEFT ? y : 2 * instruction.line() - y);
	}
}

record Instruction(Direction direction, int line) {
	static Instruction parse(String line) {
		String[] dirline = line.split(" ")[2].split("=");
		return new Instruction(dirline[0].equals("x") ? LEFT : UP, Integer.parseInt(dirline[1]));
	}
}

enum Direction {
	LEFT, UP
}
