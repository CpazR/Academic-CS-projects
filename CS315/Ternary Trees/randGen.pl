#!/usr/bin/perl -w

# Generate a stream of pseudo-random integers in the range [0 .. 2^31),
# where the initial seed is optionally specified as the first parameter.  This
# stream is not cryptographically secure, but it is adequate for Monte-Carlo
# methods.

# Author: Raphael Finkel 1/2020

use strict;

# global variables
my $seed = $ARGV[0];
my $modulus = $ARGV[1] // 2147483647; # 2^31 - 1 (never want maxint)

srand($seed) if defined $seed;
while (1) {
	print int(rand($modulus)) . "\n";
}
