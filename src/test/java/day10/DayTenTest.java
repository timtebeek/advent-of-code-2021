package day10;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
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

	private static final Map<String, Integer> pointsPerSyntaxError = new HashMap<>(Map.of(
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

	@Test
	void partTwoSample() throws Exception {
		assertThat(middleCompletionScore(SAMPLE)).isEqualTo(288957);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(middleCompletionScore(input)).isEqualTo(2995077699L);
	}

	private static long middleCompletionScore(String sample) {
		long[] scores = Stream.of(sample.split("\n"))
				.map(Line::parse)
				.map(Line::getCompletionCharacters)
				.filter(Objects::nonNull)
				.mapToLong(DayTenTest::scoreSingleCompletionLine)
				.sorted()
				.toArray();
		return LongStream.of(scores).skip(scores.length / 2).findFirst().getAsLong();
	}

	private static final Map<String, Integer> pointsPerAutoComplete = Map.of(
			")", 1,
			"]", 2,
			"}", 3,
			">", 4);

	private static long scoreSingleCompletionLine(String completionCharacters) {
		return Stream.of(completionCharacters.split(""))
				.mapToLong(pointsPerAutoComplete::get)
				.reduce((a, b) -> a * 5 + b)
				.getAsLong();
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

	public String getCompletionCharacters() {
		Deque<String> expected = new LinkedList<>();
		for (String current : characters) {
			if (pairs.containsKey(current)) {
				expected.push(pairs.get(current));
			} else if (pairs.containsValue(current)) {
				String pop = expected.pop();
				if (!pop.equals(current)) {
					return null; // Corrupted line
				}
			} else {
				throw new IllegalStateException();
			}
		}
		return expected.stream().collect(Collectors.joining());
	}

}