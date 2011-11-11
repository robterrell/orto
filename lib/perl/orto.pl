#!/usr/bin/perl
# orto.pl - Perl 4 library for connecting to Orto clients.
# Yu Kobayashi <yukoba@orto.jp>, January 2003

package orto;

# Send data
sub send {
	print "Content-type: text/html\n\n";
	print "<html><body>";
	my $var = &base64'b64encode(pack("c*", @ortoSendAry));
	chop($var);
	print $var;
	print "</body></html>";
	@ortoSendAry = ();
}

sub writeBoolean {
	if($_[0]) {
		push(@ortoSendAry, 1);
	} else {
		push(@ortoSendAry, 0);
	}
}

sub writeByte {
	push(@ortoSendAry, $_[0] & 0xff);
}

sub writeShort {
	push(@ortoSendAry, ($_[0] >> 8) & 0xff, $_[0] & 0xff);
}

# Pass argument in UTF-8
sub writeChar {
    my($utf8) = @_;
    my($c1, $c2, $c3);

	$c1 = ord($utf8);

	if(($c1 & 0x80) == 0) { # 0xxxxxxx
	    push(@ortoSendAry, $c1);
	} elsif(($c1 & 0xe0) == 0xc0) { # 110x xxxx   10xx xxxx
	    $c2 = ord(substr($utf8, 1, 1));
	    push(@ortoSendAry, (($c1 & 0x1f) << 6) | ($c2 & 0x3f));
	} else { # 1110 xxxx  10xx xxxx  10xx xxxx
	    $c2 = ord(substr($utf8, 1, 1));
	    $c3 = ord(substr($utf8, 2, 1));
	    push(@ortoSendAry, (($c1 & 0x0f) << 12) |
		     (($c2 & 0x3f) << 6) |
		     (($c3 & 0x3f) << 0));
	}
}

sub writeInt {
	my $var = $_[0] & 0xffffffff;
	push(@ortoSendAry, ($var >> 24) & 0xff, ($var >> 16) & 0xff, ($var >> 8) & 0xff, $var & 0xff);
}

sub writeLong {
	my $upper = $_[0] / 4294967296;
	if($upper < 0) {
		$upper = (int($upper) - 1) & 0xffffffff;
	} else {
		$upper = (int($upper)) & 0xffffffff;
	}
	my $lower = ($_[0] % 4294967296) & 0xffffffff;
	push(@ortoSendAry, ($upper >> 24) & 0xff, ($upper >> 16) & 0xff, ($upper >> 8) & 0xff, $upper & 0xff);
	push(@ortoSendAry, ($lower >> 24) & 0xff, ($lower >> 16) & 0xff, ($lower >> 8) & 0xff, $lower & 0xff);
}

# Pass argument in UTF-8
sub writeUTF {
	my @chars = unpack("c*", $_[0]);
	if($#chars >= 65536) {
		@chars = splice(@chars, 0, 65535);
	}

	my $var = ($#chars+1) & 0xffff;
	push(@ortoSendAry, ($var >> 8) & 0xff, $var & 0xff);
	push(@ortoSendAry, @chars);
}

# Pass argument in UTF-8
sub writeLongUTF {
	my @chars = unpack("c*", $_[0]);
	if($#chars >= 2147483648) {
		@chars = splice(@chars, 0, 2147483647);
	}

	my $var = ($#chars+1) & 0xffffffff;
	push(@ortoSendAry, ($var >> 24) & 0xff, ($var >> 16) & 0xff, ($var >> 8) & 0xff, $var & 0xff);
	push(@ortoSendAry, @chars);
}

#####################################################

$ortoNotReaded = 1;

# This method is used internally.
sub receive {
	$ortoNotReaded = 0;
	$ortoRcvPtr = 0;

	my $str;
	if ($ENV{'REQUEST_METHOD'} eq "POST") {
	    read(STDIN, $str, $ENV{'CONTENT_LENGTH'});
	} else {
	    $str = $ENV{'QUERY_STRING'};
	}
	@query = split(/&/, $str);  #####

	foreach (@query) {
	    ($name,$value) = split(/=/);

	    $value =~ tr/+/ /;
	    $value =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack("C", hex($1))/eg;
	    $name =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack("C", hex($1))/eg;

	    $query{$name} = $value;
	}

	@ortoRcvAry = unpack("C*", &base64'b64decode($query{data}));
}

sub readBoolean {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+1-1);
	return $ortoRcvAry[$ortoRcvPtr++];
}

sub readUnsignedByte {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+1-1);
	return $ortoRcvAry[$ortoRcvPtr++];
}

sub readByte {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+1-1);
	my $var = $ortoRcvAry[$ortoRcvPtr++];
	if($var >= 128) {
		$var -= 256;
	}
	return $var;
}

sub readUnsignedShort {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+2-1);
	return ($ortoRcvAry[$ortoRcvPtr++] << 8) + ($ortoRcvAry[$ortoRcvPtr++]);
}

sub readShort {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+2-1);

	my $var = ($ortoRcvAry[$ortoRcvPtr++] << 8) + ($ortoRcvAry[$ortoRcvPtr++]);
	if($var >= 32768) {
		$var -= 65536;
	}
	return $var;
}

sub readChar {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+2-1);

	my $res;
	@_ = ($ortoRcvAry[$ortoRcvPtr++] << 8, $ortoRcvAry[$ortoRcvPtr++]);
    for(@_) {
		if(($_ & 0xff80) == 0) {
		    $res .= chr($_);
		} elsif(($_ & 0xf800) == 0) {
		    $res .= chr(0xc0 | ($_ >> 6));
		    $res .= chr(0x80 | ($_ & 0x3f));
		} else {
		    $res .= chr(0xe0 | ($_ >> 12));
		    $res .= chr(0x80 | (($_ >> 6) & 0x3f));
		    $res .= chr(0x80 | ($_ & 0x3f));
		}
    }
    return $res;
}

sub readInt {
	if($ortoNotReaded) { receive(); }
	if($#ortoRcvAry < $ortoRcvPtr+4-1) { return -1; }

	my $var = ($ortoRcvAry[$ortoRcvPtr++] << 24) + ($ortoRcvAry[$ortoRcvPtr++] << 16)
		+ ($ortoRcvAry[$ortoRcvPtr++] << 8) + $ortoRcvAry[$ortoRcvPtr++];
	if($var >= 2147483648) {
		$var -= 4294967296;
	}
	return $var;
}

sub readLong {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+8-1);

	my $upper = ($ortoRcvAry[$ortoRcvPtr++] << 24) + ($ortoRcvAry[$ortoRcvPtr++] << 16)
		+ ($ortoRcvAry[$ortoRcvPtr++] << 8) + $ortoRcvAry[$ortoRcvPtr++];
	my $lower = ($ortoRcvAry[$ortoRcvPtr++] << 24) + ($ortoRcvAry[$ortoRcvPtr++] << 16)
		+ ($ortoRcvAry[$ortoRcvPtr++] << 8) + $ortoRcvAry[$ortoRcvPtr++];

	if($upper >= 2147483648) {
		$upper -= 4294967296;
	}
	
	my $var = $lower + $upper * 4294967296;
	return $var;
}

sub readUTF {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+2-1);

	my $len = ($ortoRcvAry[$ortoRcvPtr++] << 8) + $ortoRcvAry[$ortoRcvPtr++];
	return -1 if($#ortoRcvAry < $ortoRcvPtr+$len-1);

	my @chars = splice(@ortoRcvAry, $ortoRcvPtr, $len);
	return pack("C*", @chars);
}

sub readLonfUTF {
	receive() if($ortoNotReaded);
	return -1 if($#ortoRcvAry < $ortoRcvPtr+4-1);

	my $len = ($ortoRcvAry[$ortoRcvPtr++] << 24) + ($ortoRcvAry[$ortoRcvPtr++] >> 16);
		+ ($ortoRcvAry[$ortoRcvPtr++] << 8) + $ortoRcvAry[$ortoRcvPtr++];
	return -1 if($#ortoRcvAry < $ortoRcvPtr+$len-1);

	my @chars = splice(@ortoRcvAry, $ortoRcvPtr, $len);
	return pack("C*", @chars);
}

1;

#######################################################################################################

#!/usr/local/bin/perl
# base64.pl -- A perl package to handle MIME-style BASE64 encoding
# A. P. Barrett <barrett@ee.und.ac.za>, October 1993
# $Revision: 1.1.1.1 $$Date: 2003/01/23 18:36:56 $

package base64;

# Synopsis:
#       require 'base64.pl';
#
#       $uuencode_string = &base64'b64touu($base64_string);
#       $binary_string = &base64'b64decode($base64_string);
#       $base64_string = &base64'uutob64($uuencode_string);
#       $base64_string = &base64'b64encode($binary_string);
#       $uuencode_string = &base64'uuencode($binary_string);
#       $binary_string = &base64'uudecode($uuencode_string);
#
#       uuencode and base64 input strings may contain multiple lines,
#       but may not contain any headers or trailers.  (For uuencode,
#       remove the begin and end lines, and for base64, remove the MIME
#       headers and boundaries.)
#
#       uuencode and base64 output strings will be contain multiple
#       lines if appropriate, but will not contain any headers or
#       trailers.  (For uuencode, add the "begin" line and the
#       " \nend\n" afterwards, and for base64, add any MIME stuff
#       afterwards.)

####################

$base64_alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.
                   'abcdefghijklmnopqrstuvwxyz'.
                   '0123456789+/';
$base64_pad = '=';

$uuencode_alphabet = q|`!"#$%&'()*+,-./0123456789:;<=>?|.
                      '@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'; # double that '\\'!
$uuencode_pad = '`';

# Build some strings for use in tr/// commands.
# Some uuencodes use " " and some use "`", so we handle both.
# We also need to protect backslashes and other special characters.
$tr_uuencode = " ".$uuencode_alphabet;
$tr_uuencode =~ s/(\W)/\\$1/g;
$tr_base64 = "A".$base64_alphabet;
$tr_base64 =~ s/(\W)/\\$1/g;

sub b64touu
{
    local ($_) = @_;
    local ($result);

    # zap bad characters and translate others to uuencode alphabet
    eval qq{
	tr|$tr_base64||cd;
	tr|$tr_base64|$tr_uuencode|;
    };

    # break into lines of 60 encoded chars, prepending "M" for uuencode
    while (s/^(.{60})//) {
	$result .= "M" . $& . "\n";
    }

    # any leftover chars go onto a shorter line
    # with padding to the next multiple of 4 chars
    if ($_ ne "") {
	$result .= substr($uuencode_alphabet, length($_)*3/4, 1)
		   . $_
		   . ($uuencode_pad x ((60 - length($_)) % 4)) . "\n";
    }

    # return result
    $result;
}

sub b64decode
{
    local ($_) = @_;
    local ($result);

    # zap bad characters and translate others to uuencode alphabet
    eval qq{
	tr|$tr_base64||cd;
	tr|$tr_base64|$tr_uuencode|;
    };

    # break into lines of 60 encoded chars, prepending "M" for uuencode,
    # and then using perl's builtin uudecoder to convert to binary.
    while (s/^(.{60})//) {
	#warn "chunk :$&:\n";
	$result .= unpack("u", "M" . $&);
    }

    # also decode any leftover chars
    if ($_ ne "") {
	#warn "last chunk :$_:\n";
	$result .= unpack("u",
		    substr($uuencode_alphabet, length($_)*3/4, 1) . $_);
    }

    # return result
    $result;
}

sub uutob64
{
    local ($_) = @_;
    local ($result);

    # This is the most difficult, because some perverse uuencoder
    # might have made lines that do not describe multiples of 3 bytes.
    # I don't see any better method than uudecoding to binary and then
    # b64encoding the binary.

    &b64encode(&uudecode); # implicitly pass @_ to &uudecode
}

sub b64encode
{
    local ($_) = @_;
    local ($chunk);
    local ($result);

    # break into chunks of 45 input chars, use perl's builtin
    # uuencoder to convert each chunk to uuencode format,
    # then kill the leading "M", translate to the base64 alphabet,
    # and finally append a newline.
    while (s/^((.|\n){45})//) {
	#warn "in:$&:\n";
	$chunk = substr(pack("u", $&), $[+1, 60);
	#warn "packed    :$chunk:\n";
	eval qq{
	    \$chunk =~ tr|$tr_uuencode|$tr_base64|;
	};
	#warn "translated:$chunk:\n";
	$result .= $chunk . "\n";
    }

    # any leftover chars go onto a shorter line
    # with uuencode padding converted to base64 padding
    if ($_ ne "") {
	#warn "length ".length($_)." \$_:$_:\n";
	#warn "enclen ", int((length($_)+2)/3)*4 - (45-length($_))%3, "\n";
	$chunk = substr(pack("u", $_), $[+1,
			int((length($_)+2)/3)*4 - (45-length($_))%3);
	#warn "chunk:$chunk:\n";
	eval qq{
	    \$chunk =~ tr|$tr_uuencode|$tr_base64|;
	};
	#warn "translated:$chunk:\n";
	$result .= $chunk . ($base64_pad x ((60 - length($chunk)) % 4)) . "\n";
    }

    # return result
    $result;
}

sub uuencode
{
    local ($_) = @_;
    local ($result);

    # break into chunks of 45 input chars, and use perl's builtin
    # uuencoder to convert each chunk to uuencode format.
    # (newline is added by builtin uuencoder.)
    while (s/^((.|\n){45})//) {
	$result .= pack("u", $&);
    }

    # any leftover chars go onto a shorter line
    # with padding to the next multiple of 4 chars
    if ($_ ne "") {
	$result .= pack("u", $_);
    }

    # return result
    $result;
}

sub uudecode
{
    local ($_) = @_;
    local ($result);

    # use perl's builtin uudecoder to convert each line
    while (s/^([^\n]+\n?)//) {
	$result .= unpack("u", $&);
    }

    # return result
    $result;
}

1;
