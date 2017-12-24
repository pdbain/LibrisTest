package org.lasalledebain.libris.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class Lorem implements Iterator<String[]> {
	public Lorem(int minSize, int maxSize, long seed) {
		gen = new Random(seed);
		this.minSize = minSize;
		this.maxSize = maxSize;
		cursor = 0;
	}

	public static final String words[] =
		{"Phasellus", "aliquet", "diam", "id", "accumsan", "ultrices", "est", "nulla",
				"suscipit", "dui", "sit", "amet", "condimentum", "augue", "neque", "luctus",
				"libero", "Etiam", "volutpat", "imperdiet", "tempus", "Ut", "laoreet", "leo",
				"nec", "maximus", "gravida", "Interdum", "et", "malesuada", "fames", "ac",
				"ante", "ipsum", "primis", "in", "faucibus", "In", "ante", "arcu", "vulputate",
				"sit", "amet", "magna", "id", "pulvinar", "suscipit", "est", "Donec",
				"fermentum", "purus", "in", "mauris", "egestas", "eleifend", "Pellentesque",
				"vitae", "arcu", "dapibus", "pharetra", "mauris", "nec", "efficitur", "odio",
				"Cras", "laoreet", "eros", "at", "sodales", "convallis", "Donec", "varius",
				"eleifend", "lectus", "vitae", "rhoncus", "nunc", "molestie", "a", "Sed",
				"ultrices", "finibus", "orci", "eget", "auctor", "Duis", "finibus", "ante",
				"nec", "ornare", "egestas", "Donec", "nec", "consequat", "mi", "Nunc", "eget",
				"rhoncus", "dui", "Vestibulum", "ac", "tortor", "rutrum", "mattis", "tellus",
				"vel", "eleifend", "sapien", "Proin", "bibendum", "ex", "id", "consequat",
				"gravida", "dui", "sem", "venenatis", "ipsum", "sit", "amet", "tincidunt",
				"lacus", "dolor", "sed", "massa", "Sed", "volutpat", "lorem", "eget", "nibh",
				"lacinia", "aliquet", "Donec", "pellentesque", "eleifend", "venenatis",
				"Phasellus", "dictum", "hendrerit", "eleifend", "Nunc", "neque", "nisl",
				"ornare", "id", "aliquam", "ac", "aliquam", "ut", "erat", "Vivamus", "cursus",
				"nisi", "vitae", "justo", "imperdiet", "lobortis", "Etiam", "egestas", "et",
				"eros", "vel", "pellentesque", "Donec", "gravida", "a", "nisl", "sed",
				"tristique", "Phasellus", "vitae", "risus", "risus", "Sed", "ac", "ligula",
				"ut", "enim", "luctus", "pellentesque", "Donec", "volutpat", "arcu", "nec",
				"lorem", "fermentum", "eleifend", "Pellentesque", "eros", "quam", "malesuada",
				"id", "lacus", "vel", "placerat", "tempor", "lorem", "Duis", "nec", "elit",
				"posuere", "consequat", "justo", "vel", "bibendum", "lorem", "Vestibulum",
				"lobortis", "odio", "in", "risus", "faucibus", "et", "commodo", "urna",
				"pulvinar", "Vivamus", "vitae", "rutrum", "velit", "Sed", "luctus", "nibh",
				"a", "finibus", "accumsan", "dolor", "ex", "sagittis", "erat", "id",
				"pellentesque", "tellus", "odio", "non", "ipsum", "Aenean", "euismod",
				"ultricies", "sem", "et", "egestas", "Sed", "laoreet", "sapien", "in",
				"sagittis", "posuere", "nunc", "arcu", "tempor", "orci", "in", "mollis",
				"elit", "velit", "nec", "purus", "Mauris", "eget", "turpis", "sit", "amet",
				"ante", "ultricies", "mattis", "ac", "interdum", "ante", "Nulla", "rutrum",
				"nisi", "laoreet", "erat", "tempus", "sed", "volutpat", "mi", "eleifend",
				"Quisque", "quis", "pellentesque", "sem", "Lorem", "ipsum", "dolor", "sit",
				"amet", "consectetur", "adipiscing", "elit", "Pellentesque", "ut", "interdum",
				"nulla", "Nunc", "nec", "ultricies", "eros", "Donec", "in", "lorem", "at",
				"mauris", "tempus", "lobortis", "sed", "ac", "eros", "Fusce", "ultrices",
				"hendrerit", "euismod", "Cras", "et", "posuere", "eros", "at", "molestie",
				"odio", "Mauris", "in", "congue", "dolor", "Phasellus", "facilisis", "massa",
				"et", "tortor", "facilisis", "vel", "consectetur", "turpis", "posuere",
				"Morbi", "sollicitudin", "metus", "eget", "purus", "tempor", "facilisis", "Ut",
				"sit", "amet", "orci", "eget", "risus", "congue", "laoreet", "Donec",
				"sodales", "felis", "nec", "convallis", "cursus", "Fusce", "scelerisque",
				"interdum", "sagittis", "Ut", "porttitor", "enim", "non", "justo", "posuere",
				"tristique", "Quisque", "diam", "libero", "eleifend", "eu", "pellentesque",
				"id", "ultricies", "ut", "dolor", "In", "hac", "habitasse", "platea",
				"dictumst", "Integer", "facilisis", "enim", "turpis", "et", "pellentesque",
				"erat", "ultricies", "ut", "In", "hac", "habitasse", "platea", "dictumst",
				"Sed", "pretium", "eleifend", "mi", "vel", "luctus", "massa", "ultricies",
				"id", "Morbi", "ultricies", "nunc", "urna", "sed", "sollicitudin", "nibh",
				"ornare", "sed", "Phasellus", "lacinia", "dolor", "vitae", "bibendum",
				"efficitur", "Nullam", "hendrerit", "gravida", "lacus", "eu", "commodo", "Sed",
				"vulputate", "luctus", "massa", "sit", "amet", "fringilla", "Nullam",
				"porttitor", "nisi", "eu", "magna", "dignissim", "aliquam", "vel", "laoreet",
				"turpis", "Proin", "sem", "urna", "suscipit", "ac", "dui", "fringilla",
				"porta", "egestas", "nunc", "Curabitur", "congue", "elit", "et", "diam",
				"sollicitudin", "auctor", "Mauris", "malesuada", "leo", "quis", "odio",
				"maximus", "imperdiet", "Class", "aptent", "taciti", "sociosqu", "ad",
				"litora", "torquent", "per", "conubia", "nostra", "per", "inceptos",
				"himenaeos", "Nam", "libero", "nisi", "suscipit", "eu", "euismod", "a",
				"pharetra", "non", "orci", "Nullam", "eget", "ligula", "metus", "Duis", "dui",
				"mauris", "pellentesque", "id", "sapien", "at", "accumsan", "bibendum",
				"felis", "Sed", "et", "leo", "nunc", "Curabitur", "vehicula", "commodo",
				"ornare", "Ut", "sed", "fermentum", "tortor", "in", "facilisis", "erat",
				"Curabitur", "eu", "libero", "eu", "ipsum", "molestie", "euismod", "sed", "et",
				"quam", "Pellentesque", "sodales", "maximus", "ex", "non", "tristique", "dui",
				"lobortis", "at", "Aenean", "faucibus", "tellus", "eu", "est", "laoreet",
				"iaculis", "Aliquam", "finibus", "sollicitudin", "risus", "Integer",
				"tincidunt", "ex", "id", "tellus", "placerat", "aliquam", "Sed", "pretium",
				"semper", "nibh", "sed", "volutpat", "dui", "euismod", "sed", "Nam", "quis",
				"semper", "sapien", "Fusce", "imperdiet", "porttitor", "consectetur", "Sed",
				"sollicitudin", "magna", "ullamcorper", "dolor", "congue", "sit", "amet",
				"rhoncus", "nulla", "placerat", "Donec", "sit", "amet", "arcu", "id", "ligula",
				"vehicula", "sagittis", "vel", "vitae", "ex", "Mauris", "ac", "vulputate",
				"nibh", "Etiam", "massa", "dolor", "faucibus", "eu", "massa", "ac",
				"convallis", "volutpat", "mi", "Mauris", "lorem", "elit", "accumsan", "ac",
				"facilisis", "eu", "facilisis", "eget", "erat", "Ut", "vitae", "dui", "eget",
				"sem", "molestie", "vehicula", "ac", "id", "sem", "Aliquam", "egestas", "nunc",
				"erat", "id", "finibus", "felis", "blandit", "ut", "Integer", "libero", "nibh",
				"dapibus", "ac", "ante", "vitae", "imperdiet", "convallis", "orci", "Etiam",
				"commodo", "neque", "ac", "lacus", "posuere", "maximus", "Suspendisse",
				"sodales", "iaculis", "ipsum", "vel", "suscipit", "Nam", "dictum", "lorem",
				"eget", "maximus", "egestas", "quam", "nibh", "volutpat", "odio", "nec",
				"tincidunt", "dolor", "libero", "ut", "lectus", "Nullam", "placerat", "lectus",
				"id", "vestibulum", "convallis", "risus", "massa", "accumsan", "tortor", "eu",
				"dictum", "sapien", "libero", "in", "ex", "Vestibulum", "dictum", "lacinia",
				"interdum", "Donec", "eleifend", "suscipit", "ligula", "ac", "bibendum",
				"quam", "venenatis", "at", "Duis", "neque", "lorem", "iaculis", "ultrices",
				"consequat", "in", "viverra", "sed", "purus", "In", "congue", "ligula", "non",
				"placerat", "laoreet", "Cras", "augue", "leo", "pulvinar", "et", "turpis",
				"id", "ultrices", "gravida", "libero", "Nam", "eu", "arcu", "ligula", "Nullam",
				"sed", "tincidunt", "massa", "Morbi", "sit", "amet", "eros", "a", "felis",
				"lacinia", "lacinia", "a", "nec", "nibh", "Praesent", "egestas", "vestibulum",
				"orci", "Phasellus", "ut", "vulputate", "tortor", "ut", "fermentum", "ligula",
				"Aenean", "porttitor", "nibh", "sapien", "at", "dictum", "magna", "ultrices",
				"vel", "Suspendisse", "auctor", "tortor", "magna", "et", "vestibulum", "nisi",
				"venenatis", "et", "Cras", "molestie", "consectetur", "velit", "at",
				"faucibus", "Vivamus", "mi", "neque", "vulputate", "eget", "metus", "at",
				"ornare", "semper", "turpis", "Nulla", "cursus", "enim", "a", "ultrices",
				"luctus", "lectus", "odio", "convallis", "elit", "eu", "consequat", "felis",
				"neque", "pretium", "arcu", "Proin", "dapibus", "vitae", "leo", "et", "ornare",
				"Morbi", "neque", "lectus", "rhoncus", "sed", "eleifend", "non", "malesuada",
				"ac", "dui", "Pellentesque", "dictum", "at", "nisl", "sit", "amet",
				"tincidunt", "Vestibulum", "leo", "sapien", "porttitor", "in", "pharetra",
				"et", "lobortis", "et", "lorem", "Donec", "ut", "orci", "non", "lectus",
				"mollis", "pharetra", "Nulla", "dolor", "felis", "mollis", "sit", "amet",
				"quam", "non", "euismod", "lobortis", "ex", "Integer", "quis", "nisl", "eget",
				"quam", "ornare", "bibendum", "vitae", "a", "velit", "In", "eleifend",
				"iaculis", "felis", "sit", "amet", "ullamcorper", "Phasellus", "tristique",
				"rutrum", "interdum", "In", "blandit", "felis", "in", "lobortis", "venenatis",
				"Mauris", "pulvinar", "auctor", "dui", "quis", "euismod", "Integer", "eget",
				"facilisis", "mi", "ut", "fringilla", "erat", "Aenean", "sit", "amet", "quam",
				"nec", "ante", "sollicitudin", "tincidunt", "in", "facilisis", "ex", "Quisque",
				"lorem", "turpis", "convallis", "et", "egestas", "sit", "amet", "tempus", "a",
				"sem", "Aenean", "bibendum", "lacinia", "efficitur", "Quisque", "in",
				"feugiat", "augue", "et", "viverra", "erat", "Lorem", "ipsum", "dolor", "sit",
				"amet", "consectetur", "adipiscing", "elit", "Sed", "gravida", "lorem", "nec",
				"lorem", "maximus", "pellentesque", "Nulla", "ac", "mi", "sed", "purus",
				"vestibulum", "finibus", "Vivamus", "dictum", "a", "felis", "ut", "vehicula",
				"Donec", "viverra", "congue", "lorem", "ut", "iaculis", "Proin", "ac",
				"rhoncus", "odio", "Donec", "pretium", "gravida", "nisl", "id", "pulvinar",
				"Duis", "et", "leo", "nec", "diam", "consequat", "posuere", "Duis",
				"tincidunt", "rutrum", "libero", "sed", "condimentum", "Cras", "sit", "amet",
				"massa", "at", "nunc", "iaculis", "viverra"};
	int minSize;
	int maxSize;
	int cursor;
	Random gen;

	@Override
	public boolean hasNext() {
		return (cursor + minSize) < words.length;
	}

	@Override
	public String[] next() {
		int len = gen.nextInt(maxSize - minSize + 1) + minSize;
		int to = Math.min(cursor + len, words.length - 1);
		String[] result = Arrays.copyOfRange(words, cursor, to);
		cursor += len;
		return result;
	}
}
