package com.example.streampulse.data

/** Flux de test publics (peuvent changer). Chargez votre propre M3U via le bouton « Playlist ». */
object SamplePlaylist {
    const val M3U = """#EXTM3U
#EXTINF:-1 group-title="Test HLS",Apple BipBop
https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8
#EXTINF:-1 group-title="Test HLS",Mux Test Stream
https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8
#EXTINF:-1 group-title="Films",Tears of Steel
https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8
#EXTINF:-1 group-title="Live",Akamai Live Test
https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8
"""
    val channels: List<Channel> get() = M3uParser.parse(M3U)
}
