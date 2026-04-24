FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://scd4x.cfg \
            file://scd4x-overlay.dts \
            file://speaker-bonnet.cfg \
            file://speaker-bonnet-overlay.dts \
"

do_configure:append() {
    cp ${WORKDIR}/scd4x-overlay.dts ${S}/arch/arm64/boot/dts/overlays/
    if ! grep -q '^dtbo-$(CONFIG_ARCH_BCM2835) += scd4x.dtbo$' ${S}/arch/arm64/boot/dts/overlays/Makefile; then
        printf '\ndtbo-$(CONFIG_ARCH_BCM2835) += scd4x.dtbo\n' >> ${S}/arch/arm64/boot/dts/overlays/Makefile
    fi

    cp ${WORKDIR}/speaker-bonnet-overlay.dts ${S}/arch/arm64/boot/dts/overlays/
    if ! grep -q '^dtbo-$(CONFIG_ARCH_BCM2835) += speaker-bonnet.dtbo$' ${S}/arch/arm64/boot/dts/overlays/Makefile; then
        printf '\ndtbo-$(CONFIG_ARCH_BCM2835) += speaker-bonnet.dtbo\n' >> ${S}/arch/arm64/boot/dts/overlays/Makefile
    fi
}

