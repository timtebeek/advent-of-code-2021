package day10;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
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

	private static long totalSyntaxErrorScore(String input) {
		return Stream.of(input.split("\n"))
				.map(Line::parse)
				.mapToLong(Line::getCorruptScore)
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
				.mapToLong(Line::getCompletionScore)
				.filter(val -> val != 0)
				.sorted()
				.toArray();
		return LongStream.of(scores).skip(scores.length / 2).findFirst().getAsLong();
	}

}

record Line(char[] characters) {

	static Map<Character, Character> pairs = Map.of(
			'(', ')',
			'[', ']',
			'{', '}',
			'<', '>');

	static Line parse(String inputLine) {
		return new Line(inputLine.toCharArray());
	}

	private static final Map<Character, Integer> pointsPerSyntaxError = Map.of(
			')', 3,
			']', 57,
			'}', 1197,
			'>', 25137);

	public long getCorruptScore() {
		Deque<Character> expected = new LinkedList<>();
		for (char current : characters) {
			if (pairs.containsKey(current)) {
				expected.push(pairs.get(current));
			} else if (expected.pop() != current) {
				return pointsPerSyntaxError.get(current);
			}
		}
		// No corrupted character
		return 0;
	}

	public long getCompletionScore() {
		Deque<Character> expected = new LinkedList<>();
		for (char current : characters) {
			if (pairs.containsKey(current)) {
				expected.push(pairs.get(current));
			} else if (expected.pop() != current) {
				return 0; // Corrupted line
			}
		}
		return scoreSingleCompletionLine(expected);
	}

	private static final Map<Character, Integer> pointsPerAutoComplete = Map.of(
			')', 1,
			']', 2,
			'}', 3,
			'>', 4);

	private static long scoreSingleCompletionLine(Collection<Character> expected) {
		return expected.stream()
				.mapToLong(pointsPerAutoComplete::get)
				.reduce((a, b) -> a * 5 + b)
				.getAsLong();
	}

}