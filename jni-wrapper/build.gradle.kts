plugins{
    id("cpp-library")
}

library {
    linkage.set(listOf(Linkage.SHARED))
}

