<?php
/* orto.php - PHP 4 library for connecting to Orto clients.
 * This library needs PHP 4.0.6 or later.
 *
 * And you have to enable the "mbstring module".
 * !! Becare for, the mbstring module is disabled by default !!
 *
 * Yu Kobayashi <yukoba@orto.jp>, January 2003
 */

$ortoSendStr = "";

// Send data
function orto_send() {
	global $ortoSendStr;
	echo "<html><body>";
	echo base64_encode($ortoSendStr);
	echo "</body></html>";
	$ortoSendstr = "";
}

function orto_writeBoolean($b) {
	global $ortoSendStr;
	if($b) {
		$ortoSendStr .= chr(1);
	} else {
		$ortoSendStr .= chr(0);
	}
}

function orto_writeByte($b) {
	global $ortoSendStr;
	$ortoSendStr .= chr($b & 0xff);
}

function orto_writeShort($s) {
	global $ortoSendStr;
	$ortoSendStr .= chr(($s >> 8) & 0xff);
	$ortoSendStr .= chr($s & 0xff);
}

function orto_writeChar($c) {
	global $ortoSendStr;
	$ortoSendStr .= mb_convert_encoding(mb_substr($c, 0, 1), "UTF-16LE");
}

function orto_writeInt($i) {
	global $ortoSendStr;
	$ortoSendStr .= chr(($i >> 24) & 0xff);
	$ortoSendStr .= chr(($i >> 16) & 0xff);
	$ortoSendStr .= chr(($i >> 8) & 0xff);
	$ortoSendStr .= chr($i & 0xff);
}

function orto_writeLong($i) {
	global $ortoSendStr;
	$upper = $i / 4294967296;
	if($upper < 0) {
		$upper = ((int)$upper - 1) & 0xffffffff;
	} else {
		$upper = ((int)$upper) & 0xffffffff;
	}
	$lower = ($i - 4294967296 * $upper) & 0xffffffff;
	
	$ortoSendStr .= chr(($upper >> 24) & 0xff);
	$ortoSendStr .= chr(($upper >> 16) & 0xff);
	$ortoSendStr .= chr(($upper >> 8) & 0xff);
	$ortoSendStr .= chr($upper & 0xff);

	$ortoSendStr .= chr(($lower >> 24) & 0xff);
	$ortoSendStr .= chr(($lower >> 16) & 0xff);
	$ortoSendStr .= chr(($lower >> 8) & 0xff);
	$ortoSendStr .= chr($lower & 0xff);
}

function orto_writeUTF($s) {
	global $ortoSendStr;
	$utf8str = mb_convert_encoding($s, "UTF-8");
	$len = strlen($utf8str);
	if($len >= 65536) {
		$utf8str = substr($utf8str, 0, 65535);
		$len = 65535;
	}
	
	$ortoSendStr .= chr(($len >> 8) & 0xff);
	$ortoSendStr .= chr($len & 0xff);
	
	$ortoSendStr .= $utf8str;
}

function orto_writeLongUTF($s) {
	global $ortoSendStr;
	$utf8str = mb_convert_encoding($s, "UTF-8");
	$len = strlen($utf8str);
	if($len >= 2147483648) {
		$utf8str = substr($utf8str, 0, 2147483647);
		$len = 2147483647;
	}
	
	$ortoSendStr .= chr(($len >> 24) & 0xff);
	$ortoSendStr .= chr(($len >> 16) & 0xff);
	$ortoSendStr .= chr(($len >> 8) & 0xff);
	$ortoSendStr .= chr($len & 0xff);
	
	$ortoSendStr .= $utf8str;
}

// -------------------------------------------------------

$ortoRcvPtr = 0;
$ortoRcvStr = base64_decode($HTTP_POST_VARS["data"]);

function orto_readBoolean() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+1-1) { return -1; }
	if(ord($ortoRcvStr{$ortoRcvPtr++}) == 1) return true;
	else return false;
}

function orto_readUnsignedByte() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+1-1) { return -1; }
	return ord($ortoRcvStr{$ortoRcvPtr++});
}

function orto_readByte() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+1-1) { return -1; }
	$var = ord($ortoRcvStr{$ortoRcvPtr++});
	if($var >= 128) {
		$var -= 256;
	}
	return $var;
}

function orto_readUnsignedShort() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+2-1) { return -1; }
	return (ord($ortoRcvStr{$ortoRcvPtr++}) << 8) + ord($ortoRcvStr{$ortoRcvPtr++});
}

function orto_readShort() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+2-1) { return -1; }
	$var = (ord($ortoRcvStr{$ortoRcvPtr++}) << 8) + ord($ortoRcvStr{$ortoRcvPtr++});
	if($var >= 32768) {
		$var -= 65536;
	}
	return $var;
}

function orto_readChar() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+2-1) { return -1; }
	$str = substr($ortoRcvStr, $ortoRcvPtr, 2);
	$ortoRcvPtr += 2;
	return mb_convert_encoding($str, mb_internal_encoding(), "UTF-16LE");
}

function orto_readInt() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+4-1) { return -1; }
	$var = (ord($ortoRcvStr{$ortoRcvPtr++}) << 24) + (ord($ortoRcvStr{$ortoRcvPtr++}) << 16) +
		(ord($ortoRcvStr{$ortoRcvPtr++}) << 8) + ord($ortoRcvStr{$ortoRcvPtr++});
	if($var >= 2147483648) {
		$var -= 4294967296;
	}
	return $var;
}

function orto_readLong() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+4-1) { return -1; }
	$upper = (ord($ortoRcvStr{$ortoRcvPtr++}) << 24) + (ord($ortoRcvStr{$ortoRcvPtr++}) << 16) +
		(ord($ortoRcvStr{$ortoRcvPtr++}) << 8) + ord($ortoRcvStr{$ortoRcvPtr++});
	$lower = (ord($ortoRcvStr{$ortoRcvPtr++}) << 24) + (ord($ortoRcvStr{$ortoRcvPtr++}) << 16) +
		(ord($ortoRcvStr{$ortoRcvPtr++}) << 8) + ord($ortoRcvStr{$ortoRcvPtr++});
	if($upper >= 2147483648) {
		$upper -= 4294967296;
	}
	if($lower < 0) $lower += 4294967296;
	return $lower + $upper * 4294967296;
}

function orto_readUTF() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+2-1) { return -1; }
	$len =  (ord($ortoRcvStr{$ortoRcvPtr++}) << 8) + ord($ortoRcvStr{$ortoRcvPtr++});

	if(strlen($ortoRcvStr) < $ortoRcvPtr+$len-1) { return -1; }
	$str = substr($ortoRcvStr, $ortoRcvPtr, $len);
	$ortoRcvPtr += $len;
	return mb_convert_encoding($str, mb_internal_encoding(), "UTF-8");
}

function orto_readLonfUTF() {
	global $ortoRcvStr, $ortoRcvPtr;
	if(strlen($ortoRcvStr) < $ortoRcvPtr+4-1) { return -1; }
	$len = (ord($ortoRcvStr{$ortoRcvPtr++}) << 24) + (ord($ortoRcvStr{$ortoRcvPtr++}) << 16) +
		(ord($ortoRcvStr{$ortoRcvPtr++}) << 8) + ord($ortoRcvStr{$ortoRcvPtr++});

	if(strlen($ortoRcvStr) < $ortoRcvPtr+$len-1) { return -1; }
	$str = substr($ortoRcvStr, $ortoRcvPtr, $len);
	$ortoRcvPtr += $len;
	return mb_convert_encoding($str, mb_internal_encoding(), "UTF-8");
}
?>
