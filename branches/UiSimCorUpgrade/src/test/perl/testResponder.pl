#!/usr/bin/perl -w

use IO::Socket::INET;
use File::Spec;
use Cwd;
use strict;

#my ( $host, $port ) = ( "localhost", "6342" );

my ($host, $port) = ("127.0.0.1","6445");
my $cwd     = cwd();
my @dirs    = File::Spec->splitdir($cwd);
my $dropped = pop @dirs;
my $pwd     = File::Spec->catdir(@dirs);

my $socket = new IO::Socket::INET(
	PeerAddr => $host,
	PeerPort => $port,
	Proto    => 'tcp',
);

die "Connection to $host:$port failed because $!" unless defined $socket;

sendSCommand("open-session\tPerl Test Script");
receiveCommand();

sendSCommand("set-parameter\tdummySetParam\tnstep\t0");
receiveCommand();

my $increment = "0.5";
for my $i ( 1 .. 5 ) {
	my ( $sec, $min, $hour, $mday, $month, $year, $wday, $yday, $isdst ) =
	  localtime(time);
	$year += 1900;
	$month++;
	print "$month/$mday/$year";
	sendCommand( "propose	trans200912317925.320[100 23 0]"
		  . "	MDL-00-01:LBCB1	x	displacement	0.5	y	displacement	0.0"
		  . "	MDL-00-01:LBCB2	z	displacement	0.5	y	rotation	0.002" );
	receiveCommand();
	sendCommand("execute	trans200912317925.320[100 23 0]");
	receiveCommand();
	sendCommand("get-control-point	dummy	MDL-00-01:LBCB2");
	receiveCommand();
	sendCommand("get-control-point	dummy	MDL-00-01:ExternalSensors");
	receiveCommand();
	sendSCommand( "propose	trans20080206155057.44"
		  . "	MDL-00-01	x	displacement	1.0000000000e-003	y	displacement	2.0000000000e-003	z	rotation	3.0000000000e-003"
		  . "	MDL-00-02	x	displacement	4.0000000000e-003	y	displacement	5.0000000000e-003	z	rotation	6.0000000000e-003"
		  . "	MDL-00-03	x	displacement	7.0000000000e-003	y	displacement	8.0000000000e-003	z	rotation	9.0000000000e-003"
	);
	receiveCommand();
	$increment = $increment eq "0.5" ? "-0.5" : "0.5";
	sleep 1;
}

sendCommand("close-session	dummy");
receiveCommand();

close $socket;

sub sendCommand {
	my ($cmd) = @_;
	print "Sending [$cmd]\n";
	print $socket $cmd, "\015\012";
}
sub sendSCommand {
	my ($cmd) = @_;
	print "Sending [$cmd]\n";
	print $socket $cmd, "\n";
}

sub receiveCommand {
	my $result = <$socket>;
	chomp $result;
	print "Received [$result]\n";
}
