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
		assertThat(Parser.parse(SAMPLE, Type.SYNTAX).sum()).isEqualTo(26397);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(Parser.parse(input, Type.SYNTAX).sum()).isEqualTo(399153);
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
		long[] scores = Parser.parse(sample, Type.INCOMPLETE).sorted().toArray();
		return LongStream.of(scores).skip(scores.length / 2).findFirst().getAsLong();
	}

}

class Parser {

	public static LongStream parse(String input, Type type) {
		return Stream.of(input.split("\n"))
				.map(Parser::parseLine)
				.filter(error -> error.type() == type)
				.mapToLong(Error::score)
				.filter(val -> val != 0);
	}

	private static final Map<Character, Character> pairs = Map.of(
			'(', ')',
			'[', ']',
			'{', '}',
			'<', '>');

	private static final Map<Character, Integer> pointsPerSyntaxError = Map.of(
			')', 3,
			']', 57,
			'}', 1197,
			'>', 25137);

	private static Error parseLine(String line) {
		Deque<Character> expected = new LinkedList<>();
		for (char token : line.toCharArray()) {
			if (pairs.containsKey(token)) {
				expected.push(pairs.get(token));
			} else if (expected.pop() != token) {
				return new Error(Type.SYNTAX, pointsPerSyntaxError.get(token));
			}
		}
		return new Error(Type.INCOMPLETE, scoreSingleCompletionLine(expected));
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

record Error(Type type, long score) {
}

enum Type {
	SYNTAX, INCOMPLETE
}
