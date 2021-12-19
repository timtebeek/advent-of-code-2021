package day19;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

class DayNineteenTest {

	@Test
	void partOneSample() throws Exception {
		String sample = Files.readString(Paths.get(DayNineteenTest.class.getResource("sample").toURI()));
		assertThat(something(Parser.parse(sample))).isEqualTo(-1);
	}

	private static int something(List<Scanner> list) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Test
	void parserTest() throws Exception {
		String sample = Files.readString(Paths.get(DayNineteenTest.class.getResource("sample").toURI()));
		String restored = Parser.parse(sample).stream().map(Scanner::toString).collect(joining("\n"));
		assertThat(restored).isEqualTo(sample);
	}

}

class Parser {

	public static List<Scanner> parse(String input) {
		return Stream.of(input.split("\n\n")).map(Parser::scanner).toList();
	}

	private static Scanner scanner(String block) {
		String[] lines = block.split("\n");
		int id = Integer.parseInt(lines[0].split(" ")[2]);
		return new Scanner(id, Stream.of(lines).skip(1).map(Parser::position).toList());
	}

	private static Position position(String line) {
		int[] values = Stream.of(line.split(",")).mapToInt(Integer::parseInt).toArray();
		return new Position(values[0], values[1], values[2]);
	}

}

record Scanner(int id, List<Position> beacons) {

	@Override
	public String toString() {
		return """
				--- scanner %d ---
				%s
				""".formatted(id, beacons.stream().map(Position::toString).collect(joining("\n")));
	}

}

record Position(int x, int y, int z) {

	@Override
	public String toString() {
		return "%d,%d,%d".formatted(x, y, z);
	}

}
