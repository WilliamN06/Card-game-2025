package cardgame;
/*
 Immutable playing card with a non-negative integer value.
 Thread-safe due to its immutable nature
 
 */
public class Card {
        public final int denomination;

        public Card(int denomination) {
                if (denomination < 0) {
                        throw new IllegalArgumentException("Card denomination cannot be negative");
                }
                this.denomination = denomination;
        }

        public int getDenomination() {
                return denomination;
        }

        @Override
        public String toString() {
                return String.valueOf(denomination);
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj)
                        return true;
                if (obj == null || getClass() != obj.getClass())
                        return false;
                Card card = (Card) obj;
                return denomination == card.denomination;
        }

        @Override
        public int hashCode() {
                return Integer.hashCode(denomination);
        }

}
