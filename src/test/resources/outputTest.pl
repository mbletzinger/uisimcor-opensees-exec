#!/usr/bin/perl -w
use strict;
use Cwd;
our $cwd     = getcwd();
print "Current directory is \"$cwd\"\n";
our $numValues = shift;
my $old_fh = select(STDOUT);
$| = 1;
print "Writing displacements\n";
outAFile( "tmp_disp.out", 0.0001 );
print "Writing forces\n";
outAFile( "tmp_forc.out", 25.0 );

sub outAFile {
	my ( $name, $interval ) = @_;
	my $val = 0;
	my @vals;
	for my $c ( 1 .. $numValues ) {
		push @vals, $val;
		$val += $interval;
	}
	my $buf  = pack('d<*',@vals);
	open FOUT, ">$name";
	binmode FOUT;
	print FOUT $buf, "\n";
	close FOUT;
}
