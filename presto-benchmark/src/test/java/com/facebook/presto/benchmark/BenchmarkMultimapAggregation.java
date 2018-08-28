/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.benchmark;

import com.facebook.presto.spi.Page;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.testng.annotations.Test;

import java.util.List;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Thread;

@State(Thread)
@OutputTimeUnit(MILLISECONDS)
@BenchmarkMode(AverageTime)
@Fork(3)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class BenchmarkMultimapAggregation
{
    @State(Thread)
    public static class Context
    {
        private MemoryLocalQueryRunner queryRunner;

        @Param({"100", "10000", "1000000"})
        private int rowCount;

        public MemoryLocalQueryRunner getQueryRunner()
        {
            return queryRunner;
        }

        @Setup
        public void setUp()
        {
            queryRunner = new MemoryLocalQueryRunner();
        }

        @Setup(Level.Iteration)
        public void createMultimapTable()
        {
            int rows = (int) Math.sqrt(rowCount);
            queryRunner.execute(format("CREATE TABLE memory.default.multimap_agg_test AS SELECT T.A + U.A AS A, T.A+1+U.A AS B " +
                    "FROM UNNEST ( SEQUENCE (1, %d) ) AS T(A) " +
                    "CROSS JOIN UNNEST ( SEQUENCE (1, %d) ) AS U(A)",
                    rows, rows));
        }

        @TearDown(Level.Iteration)
        public void dropMultimapTable()
        {
            queryRunner.dropTable("memory.default.multimap_agg_test");
        }

        @TearDown
        public void tearDown()
        {
            queryRunner.close();
            queryRunner = null;
        }
    }

    @Benchmark
    public List<Page> benchmarkGroupBy(Context context)
    {
        return context.getQueryRunner()
                .execute("SELECT multimap_agg(T.A, T.B) FROM multimap_agg_test T GROUP BY T.A");
    }

    @Benchmark
    public List<Page> benchmarkSingle(Context context)
    {
        return context.getQueryRunner()
                .execute("SELECT multimap_agg(T.A, T.B) FROM multimap_agg_test T");
    }

    @Test
    public void verify()
    {
        Context context = new Context();
        try {
            context.setUp();
            context.createMultimapTable();

            BenchmarkMultimapAggregation benchmark = new BenchmarkMultimapAggregation();
            benchmark.benchmarkGroupBy(context);
        }
        finally {
            context.queryRunner.close();
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        // assure the benchmarks are valid before running
        new BenchmarkMultimapAggregation().verify();

        Options options = new OptionsBuilder()
                .verbosity(VerboseMode.NORMAL)
                .include(".*" + BenchmarkMultimapAggregation.class.getSimpleName() + ".*")
                .addProfiler(GCProfiler.class)
                .build();

        new Runner(options).run();
    }
}
