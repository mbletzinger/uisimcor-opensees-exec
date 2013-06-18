#!/usr/bin/perl -w
use strict;
use Cwd;
my $old_fh = select(STDOUT);
$| = 1;
select($old_fh);
print "Up and Running\n";
our $cwd     = getcwd();
print "Current directory is \"$cwd\"\n";
our ( $file1, $file2 ) = ("tmp_disp.txt","tmp_forc.txt");
our $count = 1;

print STDOUT "Starting to read STDIN\n";

while ( my $line = <STDIN> ) {
#	print STDOUT "Read \"$line\"\n";
	if ( $line =~ m!EXIT! ) {
		print STDOUT "Goodbye\n";
		last;
	}
	print STDOUT "Received \"$line\"";
	print STDOUT "\"Current step $count - done #:\"\n";
	$count++;
	sleep 2; # aggravate the ouput monitor
}

