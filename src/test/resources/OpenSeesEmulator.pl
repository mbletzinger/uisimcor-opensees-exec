#!/usr/bin/perl -w
use strict;
use Cwd;
my $old_fh = select(STDOUT);
$| = 1;
select($old_fh);
print "Up and Running\n";
our $cwd = getcwd();
print "Current directory is \"$cwd\"\n";
our ( $file1, $file2 ) = ("tmp_disp.txt","tmp_forc.txt");
our $count = 0;
our %nodes;

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
		outAFile( $file1, $count );

		#	print STDOUT "Writing to file force\n";
		outAFile( $file2, $count );
		$count++;
		print STDOUT "\"Current step $count - done #:\"\n";
	}
}

sub outAFile {
	my ( $file, $nodes ) = @_;
	my $handle;
	open $handle, ">$file";
	my $interval = 0.0001;
	my $val      = 0;
	my $noc = scalar keys(%nodes) * 3 ;
	print STDOUT "Writing $noc columns\n";
	for my $c ( 1 .. $noc ) {
		print $handle $val . " ";
		$val += $interval;
	}
	print $handle "\n";
	close $handle;
}