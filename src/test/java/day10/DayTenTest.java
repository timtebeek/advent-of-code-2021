package day10;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayTenTest {

	private static final String SAMPLE = """
			[({(<(())[]>[[{[]{<()<>>
			[(()[<>])]({[<{<<[]>>(
			{([(<{}[<>[]}>{[]{[(<()>
			(((({<>}<{<{<>}{[]{[]{}
			[[<[([]))<([[{}[[()]]]
			[{[{({}]{}}([{[{{{}}([]
			{<[[]]>}<{[{[{[]{()[[[]
			[<(<(<(<{}))><([]([]()
			<{([([[(<>()){}]>(<<{{
			<{([{{}}[<[[[<>{}]]]>[]]
			""";

	@Test
	void partOneSample() throws Exception {
		assertThat(totalSyntaxErrorScore(SAMPLE)).isEqualTo(26397);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(totalSyntaxErrorScore(input)).isEqualTo(399153);
	}

	static final Map<String, Integer> pointsPerSyntaxError = new HashMap<>(Map.of(
			")", 3,
			"]", 57,
			"}", 1197,
			">", 25137));

	private static long totalSyntaxErrorScore(String input) {
		return Stream.of(input.split("\n"))
				.map(Line::parse)
				.mapToLong(line -> pointsPerSyntaxError.getOrDefault(line.getFirstCorruptedCharacter(), 0))
				.sum();
	}

}

record Line(List<String> characters) {

	static Map<String, String> pairs = Map.of(
			"(", ")",
			"[", "]",
			"{", "}",
			"<", ">");

	static Line parse(String inputLine) {
		return new Line(List.of(inputLine.split("")));
	}

	public String getFirstCorruptedCharacter() {
		Deque<String> expected = new LinkedList<>();
		for (String current : characters) {
			if (pairs.containsKey(current)) {
				expected.push(pairs.get(current));
			} else if (pairs.containsValue(current)) {
				String pop = expected.pop();
				if (!pop.equals(current)) {
					return current;
				}
			} else {
				throw new IllegalStateException();
			}
		}
		// No corrupted character
		return null;
	}

}