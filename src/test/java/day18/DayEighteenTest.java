package day18;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayEighteenTest {

	private static final String SAMPLE = """
			[[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]
			[[[5,[2,8]],4],[5,[[9,9],0]]]
			[6,[[[6,2],[5,6]],[[7,6],[4,7]]]]
			[[[6,[0,7]],[0,9]],[4,[9,[9,0]]]]
			[[[7,[6,4]],[3,[1,3]]],[[[5,5],1],9]]
			[[6,[[7,3],[3,2]]],[[[3,8],[5,7]],4]]
			[[[[5,4],[7,7]],8],[[8,3],8]]
			[[9,3],[[9,9],[6,[4,9]]]]
			[[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]
			[[[[5,2],5],[8,[3,7]]],[[5,[7,5]],[4,4]]]
			""";

	@Test
	void partOneSample() {
		assertThat(magnitudeOfFinalSum(SAMPLE)).isEqualTo(4140);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "input", lineSeparator = "NONE")
	void partOneInput(String input) {
		assertThat(magnitudeOfFinalSum(input)).isEqualTo(-1);
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			[1,2]
			[[1,2],3]
			[9,[8,7]]
			[[1,9],[8,5]]
			[[[[1,2],[3,4]],[[5,6],[7,8]]],9]
			[[[9,[3,8]],[[0,9],6]],[[[3,7],[4,9]],3]]
			[[[[1,3],[5,3]],[[1,3],[8,7]]],[[[4,9],[6,9]],[[8,2],[7,3]]]]
			""", delimiterString = "NONE")
	void parserTest(String line) {
		assertThat(Parser.parse(line).toString()).isEqualTo(line);
	}

	static long magnitudeOfFinalSum(String input) {
		return Stream.of(input.split("\n"))
				.map(Parser::parse)
				.reduce(Number::add)
				.get()
				.magnitude();
	}
}

class Parser {

	static Number parse(String line) {
		return parse(line, 0);
	}

	private static Number parse(String line, int position) {
		char charAt = line.charAt(position);
		if (charAt == '[') { // Open
			Number left = parse(line, position + 1); // Left
			position += left.toString().length();
			if (line.charAt(position + 1) != ',') { // Comma
				throw new IllegalStateException("Expected comma'");
			}
			Number right = parse(line, position + 2); // Right
			position += 1 + right.toString().length();
			if (line.charAt(position + 1) != ']') { // Close
				throw new IllegalStateException("Expected end of pair");
			}
			return new Number(left, right);
		}
		return new Number(Character.getNumericValue(charAt)); // Plain value
	}

}

class Number {
	private Number parent;

	private Number left;
	private Number right;

	private Integer value;

	public Number(Number left, Number right) {
		this.left = left;
		this.left.parent = this;
		this.right = right;
		this.right.parent = this;
	}

	public Number(int value) {
		this.value = value;
	}

	public Number add(Number right) {
		Number paired = new Number(this, right);
		// TODO Reduce
		return paired;
	}

	public long magnitude() {
		return value != null ? value : 3 * left.magnitude() + 2 * right.magnitude();
	}

	@Override
	public String toString() {
		return value != null ? Long.toString(value) : "[%s,%s]".formatted(left, right);
	}
}
