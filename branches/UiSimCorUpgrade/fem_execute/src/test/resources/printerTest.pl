#!/usr/bin/perl -w
use strict;

our $timeSec = shift;
our $repetitions = shift;
for my $r (0 .. $repetitions) {
	print STDOUT "Printing out $r\n";
	sleep $timeSec;
	next unless $r % 3 == 0;
	print STDERR "Erroring out $r\n";
}
