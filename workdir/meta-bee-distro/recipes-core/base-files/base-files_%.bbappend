FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

do_install:append() {
    # Comment out snd_bcm2835 in /etc/modules if it exists
    if grep -q "^snd_bcm2835" ${D}${sysconfdir}/modules 2>/dev/null; then
        sed -i 's/^snd_bcm2835/#snd_bcm2835/g' ${D}${sysconfdir}/modules
    fi
}
