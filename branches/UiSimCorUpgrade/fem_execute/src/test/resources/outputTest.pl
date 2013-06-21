#!/usr/bin/perl -w
use strict;
use Cwd;
use IO::Socket::INET;
our $cwd = getcwd();
print "Current directory is \"$cwd\"\n";
our ( $numValues, $dport ) = @ARGV;
$| = 1;
our ( $dsock, $fsock, $fport );

$fport = $dport + 1;

$dsock = new IO::Socket::INET(
	PeerHost => '127.0.0.1',
	PeerPort => "$dport",
	Proto    => 'tcp',
) or die "ERROR in Socket Creation : \n";
binmode $dsock;

$fsock = new IO::Socket::INET(
	PeerHost => '127.0.0.1',
	PeerPort => "$fport",
	Proto    => 'tcp',
) or die "ERROR in Socket Creation : \n";
binmode $fsock;

print STDOUT " Writing displacements \n ";
outNum( $dsock, $numValues );
outNum( $dsock, $numValues );
outAFile( $dsock, 0.0001 );
print STDOUT " Writing forces \n ";
outNum( $fsock, $numValues );
outNum( $fsock, $numValues );
outAFile( $fsock, 25.0 );

sub outAFile {
	my ( $sock, $interval ) = @_;
	my $val = 0;
	my @vals;
	for my $c ( 1 .. $numValues ) {
		push @vals, $val;
		$val += $interval;
	}
	my $buf = pack( 'd<*', @vals );
	print $sock $buf;
}

sub outNum {
	my ( $sock, $num ) = @_;
	my $buf = pack( 'd<', $num );
	print $sock $buf;
}
