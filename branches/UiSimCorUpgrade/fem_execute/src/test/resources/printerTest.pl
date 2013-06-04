#!/usr/bin/perl -w
use strict;
use Cwd;
our $cwd = getcwd();
our $dumFile = shift;
print "Pretending to use \"$dumFile\"\n";
print "Current directory is \"$cwd\"\n";
our $timeSec = shift;
our $repetitions = shift;
$timeSec = 1 unless defined $timeSec;
$repetitions = 3 unless defined $repetitions;
for my $r (0 .. $repetitions) {
	print STDOUT "Printing out $r\n";
	sleep $timeSec;
	next unless $r % 3 == 0;
	print STDERR "Erroring out $r\n";
}
outAFile("tmp_disp.out");
outAFile("tmp_forc.out");

sub outAFile {
    my ($name) = @_;
    my $interval = 0.0001;
    open FOUT, ">$name";
    my $val = 0;
    for my $r (1 .. 20) {
        for my $c (1 .. 10) {
            print FOUT  $val . " ";
            $val += $interval;
        }
        print FOUT "\n";
    }
    close FOUT;
}
