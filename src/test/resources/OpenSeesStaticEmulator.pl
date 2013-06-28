#!/usr/bin/perl -w
use strict;
use Cwd;
our $cwd    = getcwd();
our $infile = shift;
print "Using \"$infile\"\n";
print "Current directory is \"$cwd\"\n";
our $timeSec     = 1;
our $repetitions = readInfile($infile);
for my $r ( 0 .. $repetitions ) {
	print STDOUT "Printing out $r out of $repetitions\n";
	sleep $timeSec;
	next unless $r % 3 == 0;
	print STDERR "Erroring out $r\n";
}
outAFile( "tmp_disp.out", $repetitions );
outAFile( "tmp_forc.out", $repetitions );

sub outAFile {
	my ( $name, $nodes ) = @_;
	my $interval = 0.0001;
	open FOUT, ">$name";
	my $val = 0;
	for my $c ( 1 .. ( $nodes * 3 + 1 ) ) {
		print FOUT $val . " ";
		$val += $interval;
	}
	print FOUT "\n";
	close FOUT;
}

sub readInfile {
	my ($name) = @_;
	open FIN, "<$name";
	my %nodes;
	while ( my $line = <FIN> ) {
#		print STDOUT "Scanning line \"$line\"\n";
		my ($node) = $line =~ m!^sp\s+(\d+)!;
		next unless defined $node;
		$nodes{$node} = 1;
#		print STDOUT "Found node $node\n";
	}
	return scalar keys(%nodes);
}
