FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://scd4x.cfg \
            file://scd4x-overlay.dts \
"

do_configure:append() {
    cp ${WORKDIR}/scd4x-overlay.dts ${S}/arch/arm/boot/dts/overlays

    if ! grep -q '^dtbo-$(CONFIG_ARCH_BCM2835) += scd4x.dtbo$' ${S}/arch/arm/boot/dts/overlays/Makefile; then
        printf '\ndtbo-$(CONFIG_ARCH_BCM2835) += scd4x.dtbo\n' >> ${S}/arch/arm/boot/dts/overlays/Makefile
    fi
}
