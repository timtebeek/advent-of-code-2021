package day02;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayTwoTest {

	private static final String[] SAMPLE = """
			forward 5
			down 5
			forward 8
			up 3
			down 8
			forward 2
			""".split("\n");

	@Test
	void partOneSample() throws Exception {
		assertThat(multiplyFinalHorizontalPositionAndDepth(Stream.of(SAMPLE))).isEqualTo(150);
	}

	@Test
	void partOneInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(multiplyFinalHorizontalPositionAndDepth(lines)).isEqualTo(2027977);
	}

	private static int multiplyFinalHorizontalPositionAndDepth(Stream<String> lines) {
		List<Move> moves = lines.map(line -> line.split(" "))
				.map(split -> new Move(split[0], Integer.parseInt(split[1])))
				.toList();
		int horizontalPosition = moves.stream()
				.filter(move -> move.direction().equals("forward"))
				.mapToInt(Move::units)
				.sum();
		int depth = moves.stream()
				.filter(move -> !move.direction().equals("forward"))
				.mapToInt(move -> move.direction().equals("up") ? 0 - move.units() : move.units())
				.sum();
		return horizontalPosition * depth;
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(multiplyFinalHorizontalPositionAndDepthWithAim(Stream.of(SAMPLE))).isEqualTo(900);
	}

	@Test
	void partTwoInput() throws Exception {
		Stream<String> lines = Files.lines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(multiplyFinalHorizontalPositionAndDepthWithAim(lines)).isEqualTo(1903644897);
	}

	private static int multiplyFinalHorizontalPositionAndDepthWithAim(Stream<String> lines) {
		return lines.map(line -> line.split(" "))
				.map(split -> new Move(split[0], Integer.parseInt(split[1])))
				.reduce(new Position(0, 0, 0),
						Position::apply,
						Position::combine) // Combiner not used in practice, as we're not running in parallel
				.multiplied();
	}

}

record Move(
		String direction,
		int units) {
}

record Position(
		int aim,
		int horizontal,
		int depth) {

	Position apply(Move move) {
		int units = move.units();
		return switch (move.direction()) {
		case "forward" -> new Position(aim, horizontal + units, depth + aim * units);
		case "up" -> new Position(aim - units, horizontal, depth);
		case "down" -> new Position(aim + units, horizontal, depth);
		default -> throw new IllegalArgumentException("Unexpected value: " + move.direction());
		};
	}

	Position combine(Position b) {
		return new Position(aim + b.aim(), horizontal + b.horizontal(), depth + b.depth());
	}

	int multiplied() {
		return horizontal * depth;
	}

}
