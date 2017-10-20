#include "dominion.h"
#include "dominion_helpers.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "rngs.h"

/*
 * NOTE: card behavior based on module notes from the instructor and the wiki
 *   http://wiki.dominionstrategy.com/index.php/Adventurer
 * NOTE: test setup influenced by the steward sample test case from instructor
 *
 * test suite for the adventurer card
 * card behavior: play the adventurer card. keep revealing cards from your deck
 *   until you find 2 treasure cards, which you add to your hand. discard the
 *   other revealed cards
 * test setup: call initializeGame with 2 players, random seed of 10.
 * tests
 *   player 1 deck has x fewer cards
 *   player 1 hand has 2 more cards
 *   player 1 played 1 card
 *   player 2 deck unchanged
 *   coins are unchanged
 *   player 1 discards 7 cards
 *   supply pile counts are unchanged
 */

void printResult(int expected, int actual, char *tmp);

int main (int argc, char** argv) {
    struct gameState pre, post;
    int numPlayers = 2;
    int playerOne = 0;
    int playerTwo = 1;
    /* set args for card effect per line 88 in playdom.c */
    int handPos = 0, choice1 = -1, choice2 = -1, choice3 = -1, bonus = 0;
    int seed = 10;
    int k[] = {adventurer, gardens, embargo, village, minion, mine, cutpurse,
	sea_hag, tribute, smithy};

    int i;
    int expected, actual;
    int mismatch = 0;
    int numNewCardsInHand = 2;
    int numPlayedCards = 1;

    /* initialize 2 player game with seed 10 */
    initializeGame(numPlayers, k, seed, &pre);

    /* copy the game state, use post for testing, and compare to pre */
    post = pre;

    printf("test suite for playing adventurer\n");
    cardEffect(adventurer, choice1, choice2, choice3, &post, handPos, &bonus);

    expected = pre.handCount[playerOne] + numNewCardsInHand - numPlayedCards;
    actual = post.handCount[playerOne];
    printResult(expected, actual, "Player 1 hand count");

    /* expected = pre.deckCount[playerOne] - numNewCardsInHand; */
    /* actual = post.deckCount[playerOne]; */
    /* printResult(expected, actual, "Player 1 deck count"); */

    expected = pre.playedCardCount + numPlayedCards;
    actual = post.playedCardCount;
    printResult(expected, actual, "Player 1 played card count");

    expected = pre.deckCount[playerTwo];
    actual = post.deckCount[playerTwo];
    printResult(expected, actual, "Player 2 deck count");

    expected = pre.coins;
    actual = post.coins;
    printResult(expected, actual, "Coin count");

    endTurn(&post);

    expected = numNewCardsInHand + pre.handCount[playerOne] - numPlayedCards;
    actual = post.discardCount[playerOne];
    printResult(expected, actual, "Player 1 discard count");

    printf("Supply counts: ");
    for (i = 0; i < treasure_map + 1; i++) {
	if (pre.supplyCount[i] != post.supplyCount[i]) {
	    mismatch = 1;
	    break;
	}
    }
    if (!mismatch) {
	printf("PASSED\n");
    } else {
	printf("FAILED: Supply counts don't match at card %d\n", i);
    }

    return 0;
}

void printResult(int expected, int actual, char *tmp) {
    char *testName = tmp;
    if (expected == actual) {
	printf("PASSED: %s", testName);
    } else {
	printf("FAILED: %s - expected %d, got %d", testName, expected, actual);
    }
    printf("\n");
}
