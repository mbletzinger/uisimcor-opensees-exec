#!/usr/bin/perl -w
use strict;
use Cwd;
my $old_fh = select(STDOUT);
$| = 1;
select($old_fh);
print "Up and Running\n";
our $cwd = getcwd();
print "Current directory is \"$cwd\"\n";
our ( $fout1, $fout2 );
our $count = 0;
open $fout1, ">>tmp_disp.out";
open $fout2, ">>tmp_forc.out";
our %nodes;

$old_fh = select($fout1);
$|      = 1;
select($fout1);
$old_fh = select($fout2);
$|      = 1;
select($fout2);
print STDOUT "Starting to read STDIN\n";

while ( my $line = <STDIN> ) {

	#	print STDOUT "Read \"$line\"\n";
	if ( $line =~ m!EXIT! ) {
		print STDOUT "Goodbye\n";
		last;
	}
	print STDOUT "Received \"$line\"";
	my ($node) = $line =~ m!^sp\s+(\d+)!;
	if ( defined $node ) {
		$nodes{$node} = 1;
		print STDOUT "Found node $node\n";
	}
	if ( $line =~ m!done\s+#:! ) {

		#	print STDOUT "Writing to file disp\n";
		outAFile( $fout1, $count );

		#	print STDOUT "Writing to file force\n";
		outAFile( $fout2, $count );
		$count++;
		print STDOUT "\"Current step $count - done #:\"\n";
	}
}

sub outAFile {
	my ( $handle, $nodes ) = @_;
	my $interval = 0.0001;
	my $val      = 0;
	my $noc = scalar keys(%nodes) * 3 ;
	print STDOUT "Writing $noc columns\n";
	for my $c ( 1 .. $noc ) {
		print $handle $val . " ";
		$val += $interval;
	}
	print $handle "\n";
}

