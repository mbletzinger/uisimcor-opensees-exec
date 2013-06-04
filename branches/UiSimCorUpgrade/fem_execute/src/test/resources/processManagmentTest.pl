#!/usr/bin/perl -w
use strict;
use Cwd;
my $old_fh = select(STDOUT);
$| = 1;
select($old_fh);
print "Up and Running\n";
our $cwd     = getcwd();
print "Current directory is \"$cwd\"\n";
our $timeSec     = shift;
our $repetitions = shift;
$timeSec     = 1 unless defined $timeSec;
$repetitions = 3 unless defined $repetitions;
our ( $fout1, $fout2 );
our $count = 1;
open $fout1, ">>tmp_disp.out";
open $fout2, ">>tmp_forc.out";

$old_fh = select($fout1);
$| = 1;
select($fout1);
$old_fh = select($fout2);
$| = 1;
select($fout2);
print STDOUT "Starting to read STDIN\n";

while ( my $line = <STDIN> ) {
#	print STDOUT "Read \"$line\"\n";
	if ( $line =~ m!EXIT! ) {
		print STDOUT "Goodbye\n";
		last;
	}
#	print STDOUT "Received \"$line\"";
#	print STDOUT "Writing to file disp\n";
	outAFile( $fout1, $count );
#	print STDOUT "Writing to file force\n";
	outAFile( $fout2, $count );
	$count++;
	print STDOUT "\"Current step $count - done #:\"\n";
}

sub outAFile {
	my ( $handle, $step ) = @_;
	my $interval = 0.0001;
	my $val      = $step;
	for my $c ( 1 .. 10 ) {
		print $handle $val . " ";
		$val += $interval;
	}
	print $handle "\n";
}