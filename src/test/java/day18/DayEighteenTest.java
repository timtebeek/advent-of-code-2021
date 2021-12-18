package day18;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(magnitudeOfFinalSum(input)).isEqualTo(4120);
	}

	static long magnitudeOfFinalSum(String input) {
		return Parser.parse(input)
				.reduce(Number::add)
				.get()
				.magnitude();
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(largestMagnitudeOfTwoNumbers(SAMPLE)).isEqualTo(3993);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(largestMagnitudeOfTwoNumbers(input)).isEqualTo(4725);
	}

	private static long largestMagnitudeOfTwoNumbers(String input) {
		long max = 0;
		List<String> numbers = List.of(input.split("\n"));
		for (String a : numbers) {
			for (String b : numbers) {
				if (a.equals(b)) {
					continue;
				}
				// Parse numbers each time, as Number is mutable
				long magnitude = Parser.parse(a, 0).add(Parser.parse(b, 0)).magnitude();
				if (max < magnitude) {
					max = magnitude;
				}
			}
		}
		return max;
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			[[1,2],[[3,4],5]]=143
			[[[[0,7],4],[[7,8],[6,0]]],[8,1]]=1384
			[[[[1,1],[2,2]],[3,3]],[4,4]]=445
			[[[[3,0],[5,3]],[4,4]],[5,5]]=791
			[[[[5,0],[7,4]],[5,5]],[6,6]]=1137
			[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]=3488
			[[[[6,6],[7,6]],[[7,7],[7,0]]],[[[7,7],[7,7]],[[7,8],[9,9]]]]=4140
			[[[[7,8],[6,6]],[[6,0],[7,7]]],[[[7,8],[8,8]],[[7,9],[0,6]]]]=3993
			""", delimiter = '=')
	void magnitudeTest(String line, long magnitude) {
		assertThat(Parser.parse(line, 0).magnitude()).isEqualByComparingTo(magnitude);
	}
}

class Parser {

	public static Stream<Number> parse(String input) {
		return Stream.of(input.split("\n"))
				.map(line -> parse(line, 0));
	}

	static Number parse(String line, int position) {
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

	public Number add(Number added) {
		Number root = new Number(this, added);

		while (true) {
			List<Number> numbersInOrder = root.stream().toList();
			Optional<Number> firstExploding = numbersInOrder.stream()
					.filter(Predicate.not(Number::isRegularNumber))
					.filter(n -> n.depth() == 5)
					.findFirst();
			if (firstExploding.isPresent()) {
				Number exploding = firstExploding.get();
				firstRegularNumberToTheLeft(numbersInOrder, exploding)
						.ifPresent(n -> n.value = n.value + exploding.left.value);
				firstRegularNumberToTheRight(numbersInOrder, exploding)
						.ifPresent(n -> n.value = n.value + exploding.right.value);
				exploding.parent.replace(exploding, new Number(0));
				continue;
			}
			Optional<Number> firstSplit = numbersInOrder.stream()
					.filter(Number::isRegularNumber)
					.filter(n -> 10 <= n.value)
					.findFirst();
			if (firstSplit.isPresent()) {
				Number splitting = firstSplit.get();
				Number pair = new Number(
						new Number(splitting.value / 2),
						new Number((splitting.value + 1) / 2));
				// Set parent here for regular numbers
				splitting.parent.replace(splitting, pair);
				continue;
			}
			break;
		}
		return root;
	}

	private static Optional<Number> firstRegularNumberToTheLeft(List<Number> numbersInOrder, Number explodingPair) {
		int indexOf = numbersInOrder.indexOf(explodingPair.left);
		while (0 <= --indexOf) {
			Number leaf = numbersInOrder.get(indexOf);
			if (leaf.isRegularNumber()) {
				return Optional.of(leaf);
			}
		}
		return Optional.empty();
	}

	private static Optional<Number> firstRegularNumberToTheRight(List<Number> numbersInOrder, Number explodingPair) {
		int indexOf = numbersInOrder.indexOf(explodingPair.right);
		while (++indexOf < numbersInOrder.size()) {
			Number leaf = numbersInOrder.get(indexOf);
			if (leaf.isRegularNumber()) {
				return Optional.of(leaf);
			}
		}
		return Optional.empty();
	}

	private void replace(Number replaced, Number with) {
		with.parent = this;
		if (left.equals(replaced)) {
			left = with;
		} else if (right.equals(replaced)) {
			right = with;
		} else {
			throw new IllegalStateException("Neither left nor right match " + replaced + " in " + this);
		}
	}

	private int depth() {
		int depth = 1;
		Number current = this;
		while (current.parent != null) {
			current = current.parent;
			depth++;
		}
		return depth;
	}

	private boolean isRegularNumber() {
		return value != null;
	}

	private Stream<Number> stream() {
		return isRegularNumber()
				? Stream.of(this)
				: Stream.concat(
						left.stream(),
						Stream.concat(
								Stream.of(this),
								right.stream()));
	}

	public long magnitude() {
		return isRegularNumber() ? value : 3 * left.magnitude() + 2 * right.magnitude();
	}

	@Override
	public String toString() {
		return isRegularNumber() ? Long.toString(value) : "[%s,%s]".formatted(left, right);
	}
}
