import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

public class CommonPublishersTask1Tests {

	@Test
	public void testCorrectFluxTypeUsed() {
		ArrayList<Publisher<?>> publishers = new ArrayList<>();
		Hooks.onEachOperator(p -> {
			publishers.add(p);
			return p;
		});

		try {
			Flux<Integer> sequence = Task.createSequence();

			sequence.subscribe();
		}
		finally {
			Hooks.resetOnEachOperator();
		}

		Assertions.assertThat(publishers)
		          .first()
		          .as("Used incorrect Flux type")
		          .hasSameClassAs(Flux.range(0, 20));
	}

	@Test
	public void testResultIsCorrect() {
		Flux<Integer> sequence = Task.createSequence();

		StepVerifier.create(sequence)
		            .recordWith(ArrayList::new)
		            .expectNextCount(20)
		            .consumeRecordedWith(r -> {
			            Assertions.assertThat(r)
			                      .hasSize(20);
			            Iterator<Integer> iterator = r.iterator();
			            for (int i = 0; i < 20; i++) {
				            Integer next = iterator.next();
				            Assertions.assertThat(next)
				                      .as("Expected sequence of elements from 0 to 20 but got element [%d] out of order",
						                      next)
				                      .isEqualTo(i);
			            }
		            })
		            .expectComplete()
		            .verify(Duration.ofMillis(100));
	}
}