#!/usr/bin/perl -w
use strict;
use Cwd;
use IO::Socket::INET;
$| = 1;
print STDOUT "Up and Running\n";
our $cwd = getcwd();
print STDOUT "Current directory is \"$cwd\"\n";
our ( $file1, $file2 ) = ( "tmp_disp.out", "tmp_forc.out" );
our $count = 0;
our %nodes;

print STDOUT "Starting to read STDIN\n";
our ( $dsock, $fsock, $dport, $fport );
our $numRecords = 10;

while ( my $line = <STDIN> ) {

	#	print STDOUT "Read \"$line\"\n";
	if ( $line =~ m!EXIT! ) {
		print STDOUT "Goodbye\n";
		last;
	}

	print STDOUT "Received \"$line\"";
	unless ( defined $dport ) {
		my ($dport) = $line =~ m!127.0.0.1\s+(\d+).+disp!;
		if ( defined $dport ) {
			print STDOUT "found disp port $dport\n";
			$dsock = createSocket($dport);

			#			print STDERR "Writing disp port $dport\n";
			outNum( $dsock, $dport );
		}
	}
	unless ( defined $fport ) {
		my ($fport) = $line =~ m!127.0.0.1\s+(\d+).+reaction!;
		if ( defined $fport ) {
			print STDOUT "found reaction port $fport\n";
			$fsock = createSocket($fport);

			#			print STDERR "Writing force port $fport\n";
			outNum( $fsock, $fport );
		}
	}
	my ($node) = $line =~ m!^sp\s+(\d+)!;
	if ( defined $node ) {
		$nodes{$node} = 1;
		print STDOUT "Found node $node\n";
	}
	if ( $line =~ m!done\s+#:! ) {

		print STDOUT "Writing values\n";
		for my $i ( 1 .. $numRecords ) {
			outAFile($dsock);
		}
		     #	print STDOUT "Writing to file force\n";
		for my $i ( 1 .. $numRecords ) {
			outAFile($fsock);
		}
		$count++;
		print STDOUT "\"Current step $count - done #:\"\n";
	}

	# print STDOUT "Waiting for input\n";
}
print STDOUT "Ok I'm Leaving\n";

sub outAFile {
	my ($sock)   = @_;
	my $interval = 0.0001;
	my $val      = 0;
	my @vals;
	my $noc = 1 + scalar keys(%nodes) * 3;
	print STDOUT "Writing $noc columns\n";

	#	print STDERR "Writing noc $noc\n";
	outNum( $sock, $noc );

	for my $c ( 1 .. $noc ) {
		push @vals, $val;
		$val += $interval;
	}
	my $buf = pack( 'd<*', @vals );
	print $sock $buf;
}

sub createSocket {
	my ($port) = @_;
	my $sock;
	$sock = new IO::Socket::INET(
		PeerHost => '127.0.0.1',
		PeerPort => "$port",
		Proto    => 'tcp',
	) or die "ERROR in Socket Creation : \n";
	binmode $sock;
	print STDOUT "Created socket on port [$port]\n";
	return $sock;

}

sub outNum {
	my ( $sock, $num ) = @_;
	my $buf = pack( 'd<', $num );
	print $sock $buf;
}

