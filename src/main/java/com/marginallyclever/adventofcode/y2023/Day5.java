package com.marginallyclever.adventofcode.y2023;

    import java.io.BufferedInputStream;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Objects;

    public class Day5 {
        static class MyRange {
            public long from;
            public long to;
            public long range;

            public MyRange(long from,long to,long range) {
                this.from = from;
                this.to = to;
                this.range = range;
            }
        }
        static class MyMap {
            public String name;
            public List<MyRange> ranges = new ArrayList<>();

            public MyMap(String name) {
                this.name = name;
            }

            public long getResult(long input) {
                for(MyRange r : ranges) {
                    if(input>=r.from && input<r.from+r.range) {
                        //System.out.println("map "+name+" "+input+" -> "+(input-r.from+r.to));
                        return input - r.from + r.to;
                    }
                }
                return input;
            }
        };

        private long [] seeds;
        private final List<MyMap> maps = new ArrayList<>();
        private MyMap latest = null;

        public static void main(String[] args) {
            Day5 me = new Day5();
            me.processFile("input.txt");
            long t0 = System.nanoTime();
            me.findLocations1();
            long t1 = System.nanoTime();
            me.findLocations2();
            long t2 = System.nanoTime();
            System.out.println("time 1 = "+(t1-t0)/1000000+"ms");
            System.out.println("time 2 = "+(t2-t1)/1000000+"ms");
        }

        private void findLocations1() {
            long smallest = Long.MAX_VALUE;
            for(int i=0;i<seeds.length;++i) {
                long input = seeds[i];
                System.out.println("seed "+i+" = "+input);
                long result = findLocationForSeed(input);
                if(result<smallest) smallest = result;
            }
            System.out.println("smallest 1 = "+smallest);
        }

        private void findLocations2() {
            long smallest = Long.MAX_VALUE;
            for(int i=0;i<seeds.length;++i) {
                long input = seeds[i];
                long range = seeds[++i];
                System.out.println("seed "+i+" = "+input+" range "+range);
                for(long j=0;j<range;++j) {
                    long result = findLocationForSeed(input+j);
                    if(result<smallest) smallest = result;
                }
            }
            System.out.println("smallest 2 = "+smallest);
        }

        private long findLocationForSeed(long input) {
            for(MyMap m : maps) {
                input = m.getResult(input);
                //System.out.println("map "+m.name+" = "+input);
            }
            return input;
        }

        private void processFile(String filename) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(Day5.class.getResourceAsStream(filename)))))) {
                String line;
                while ((line = br.readLine()) != null) {
                    processLine(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processLine(String line) {
            if(line.trim().isEmpty()) return;

            if(line.startsWith("seeds: ")) {
                String [] list = line.substring(7).split(" ");
                seeds = new long[list.length];
                for(int i=0;i<list.length;++i) {
                    seeds[i] = Long.parseLong(list[i]);
                }
                return;
            }

            if(line.endsWith("map:")) {
                latest = new MyMap(line.substring(0,line.length()-5));
                maps.add(latest);
                return;
            }

            // must be three numbers separated by spaces
            String [] list = line.split(" ");
            if(list.length!=3) {
                System.out.println("Error: line must be three numbers separated by spaces.");
                return;
            }
            MyRange r = new MyRange(Long.parseLong(list[1]),
                                    Long.parseLong(list[0]),
                                    Long.parseLong(list[2]));
            latest.ranges.add(r);
        }
    }